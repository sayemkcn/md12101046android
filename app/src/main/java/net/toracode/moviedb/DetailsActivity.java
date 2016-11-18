package net.toracode.moviedb;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.facebook.accountkit.AccountKit;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import net.toracode.moviedb.adapters.PersonsRecyclerAdapter;
import net.toracode.moviedb.commons.CustomListOperations;
import net.toracode.moviedb.commons.Pref;
import net.toracode.moviedb.entity.CustomList;
import net.toracode.moviedb.entity.Movie;
import net.toracode.moviedb.entity.Person;
import net.toracode.moviedb.fragments.ReviewFragment;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DetailsActivity extends AppCompatActivity {
    @BindView(R.id.trailerVideoView)
    VideoView trailerVideoView;
    @BindView(R.id.movieNameTextView)
    TextView movieNameTextView;
    @BindView(R.id.producerNameTextView)
    TextView producerTextView;
    @BindView(R.id.filmRatingTextView)
    TextView filmRatingTextView;
    @BindView(R.id.playButton)
    ImageButton playButton;
    @BindView(R.id.detailsScrollView)
    ScrollView detailsScrollView;
    @BindView(R.id.averageRatingBar)
    RatingBar averageRatingBar;
    @BindView(R.id.posterImageView)
    ImageView posterImageView;
    @BindView(R.id.genereTextView)
    TextView genereTextView;
    @BindView(R.id.durationTextView)
    TextView durationTextView;
    @BindView(R.id.releaseDateTextView)
    TextView releaseDateTextView;
    @BindView(R.id.storyLineTextView)
    TextView storyLineTextView;
    @BindView(R.id.industryTextView)
    TextView movieIndustryTextView;
    @BindView(R.id.movieLanguageTextView)
    TextView movieLanguageTextView;
    @BindView(R.id.castAndCrewRecyclerView)
    RecyclerView castAndCrewRecyclerView;

    @BindView(R.id.contentLayout)
    View contentLayout;
    @BindView(R.id.detailsProgressBar)
    ProgressBar progressBar;
    @BindView(R.id.reviewFragmentContainer)
    View reviewFragmentContainer;

    private GestureDetector mGestureDitector;
    private MediaController mc;

    private Movie movie;

    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // register eventbus
        ButterKnife.bind(this);

        this.typeface = typeface.createFromAsset(getAssets(), "fonts/SolaimanLipi.ttf");

        this.movie = (Movie) getIntent().getExtras().getSerializable("movie");

        this.updateViews(this.movie);

        // load cast and crew list
        this.loadCastAndCrews(this.movie.getUniqueId());

        // VIDEO VIEW
        this.showVideo(movie.getTrailerUrl());
        this.setListeners();

        // REVIEW FRAGMENT
        ReviewFragment reviewFragment = new ReviewFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("movieId", movie.getUniqueId());
        reviewFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.reviewFragmentContainer, reviewFragment).commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AccountKit.getCurrentAccessToken() == null)
                    startActivity(new Intent(DetailsActivity.this, PreferenceActivity.class));
                else
                    fetchListsAndShowChooserDialog(AccountKit.getCurrentAccessToken().getAccountId());
            }
        });
    }

    private void loadCastAndCrews(Long movieId) {
        final String url = getResources().getString(R.string.baseUrl) + "person/movie/" + movieId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                ResponseBody responseBody = null;
                try {
                    response = new ResourceProvider(DetailsActivity.this).fetchGetResponse(url);
                    responseBody = response.body();
                    String responseBodyString = responseBody.string();
                    if (response.code() == ResourceProvider.RESPONSE_CODE_OK) {
                        final List<Person> personList = parsePersonList(responseBodyString);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setupRecyclerView(personList);
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e("FETCH_CAST_LIST", e.toString());
                } finally {
                    if (responseBody != null)
                        responseBody.close();
                    if (response != null)
                        response.close();
                }
            }
        }).start();
    }

    private List<Person> parsePersonList(String responseBodyString) {
        List<Person> personList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseBodyString);
            Gson gson = Commons.buildGson();
            for (int i = 0; i < jsonArray.length(); i++) {
                Person person = gson.fromJson(jsonArray.getJSONObject(i).toString(), Person.class);
                personList.add(person);
            }
        } catch (JSONException e) {
            Log.e("PARSE_CAST_LIST", e.toString());
        }
        return personList;
    }

    private void setupRecyclerView(List<Person> personList) {
        castAndCrewRecyclerView.setAdapter(new PersonsRecyclerAdapter(this, personList));
        castAndCrewRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setListeners() {
        this.trailerVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                trailerVideoView.seekTo(100);
            }
        });
        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trailerVideoView.start();
                playButton.setVisibility(View.GONE);
            }
        });
        this.detailsScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                mc.hide();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        } else if (id == R.id.action_save) {
            this.saveOffline(Pref.PREF_KEY_OFFLINE_LIST);
        } else if (id == R.id.action_add_to_list) {
            if (AccountKit.getCurrentAccessToken() == null)
                this.startActivity(new Intent(this, PreferenceActivity.class));
            else
                this.fetchListsAndShowChooserDialog(AccountKit.getCurrentAccessToken().getAccountId());
        }
        return super.onOptionsItemSelected(item);
    }

    private void showVideo(String url) {
        Uri uri = Uri.parse(url);
        this.mc = new MediaController(this);
//        mc.setAnchorView(this.trailerVideoView);
        this.trailerVideoView.setMediaController(mc);
        this.trailerVideoView.setVideoURI(uri);
        this.trailerVideoView.seekTo(100);
        this.trailerVideoView.requestFocus();

    }

    // Fetch lists from the server
    private void fetchListsAndShowChooserDialog(String accountId) {
        final SuperToast loadingToast = Commons.getLoadingToast(this);
        loadingToast.show();

        final String url = getResources().getString(R.string.baseUrl) + "list?accountId=" + accountId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(DetailsActivity.this).fetchGetResponse(url);
                    final ResponseBody responseBody = response.body();
                    final String responseBodyString = responseBody.string();
                    responseBody.close(); // lol. closed the fucking connection.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (loadingToast.isShowing()) loadingToast.dismiss();
                            if (response.code() == ResourceProvider.RESPONSE_CODE_BAD_REQUEST) {
                                Commons.showSimpleToast(getApplicationContext(), "You\'re not logged in. Please login to continue.");
                                startActivity(new Intent(DetailsActivity.this, PreferenceActivity.class));
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_NOT_FOUND) { // if list is empty for the user.
                                new CustomListOperations(DetailsActivity.this).createCustomList("Watchlist", "This is your private watchlist. This list auto generated for you.", "private");
                                List<CustomList> listOfCustomList = parseCustomList(responseBodyString);
                                showAddToListDialog(listOfCustomList);
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_FOUND) { // if list is not empty then show a chooser dialog
                                List<CustomList> listOfCustomList = parseCustomList(responseBodyString);
                                showAddToListDialog(listOfCustomList);
                            }
                        }
                    });

                } catch (IOException e) {
                    Log.e("FETCH_LIST", e.toString());
                }
            }
        }).start();
    }

    // shows user custom list chooser.
    private void showAddToListDialog(final List<CustomList> listOfCustomList) {
        String[] listTitles = this.copyListTitles(listOfCustomList);

        new MaterialDialog.Builder(this)
                .title("Add To List")
                .items(listTitles)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        CustomList list = listOfCustomList.get(which);
                        addMovieToList(list, movie);
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        return true;
                    }
                })
                .positiveText("Add")
                .negativeText("Cancel")
                .neutralText("New List")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        new CustomListOperations(DetailsActivity.this).showNewListDialog();
                    }
                })
                .canceledOnTouchOutside(false)
                .show();
    }

    private void addMovieToList(final CustomList list, final Movie movie) {
        final String url = getResources().getString(R.string.baseUrl) + "list/" + list.getUniqueId() + "/add/" + movie.getUniqueId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(DetailsActivity.this).fetchPostResponse(url);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_INTERNAL_SERVER_ERROR) {
                                Commons.showSimpleToast(getApplicationContext(), "Can not add " + movie.getName() + " to list. List not found! ");
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_CONFLICT) {
                                Commons.showDialog(DetailsActivity.this, "Can not add movie!", "Movie " + movie.getName() + " is already added to your " + list.getTitle() + ".");
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_OK) {
                                Commons.showDialog(DetailsActivity.this, "Success!", "Movie " + movie.getName() + " is added to your " + list.getTitle() + " successfully.");
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e("ADD_TO_LIST", e.toString());
                }
            }
        }).start();
    }

    private String[] copyListTitles(List<CustomList> listOfCustomList) {
        String[] listTitles = new String[listOfCustomList.size()];
        for (int i = 0; i < listTitles.length; i++) {
            listTitles[i] = listOfCustomList.get(i).getTitle();
        }
        return listTitles;
    }

    private List<CustomList> parseCustomList(String jsonArrayString) {
        // Creates the json object which will manage the information received
        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        Gson gson = builder.create();
        List<CustomList> listOfCustomList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for (int i = 0; i < jsonArray.length(); i++) {
                CustomList list = gson.fromJson(jsonArray.getJSONObject(i).toString(), CustomList.class);
                listOfCustomList.add(list);
            }
        } catch (JSONException e) {
            Log.e("LIST_JSON_PERSON", e.toString());
        }
        return listOfCustomList;
    }


    private void saveOffline(String key) {
        if (this.movie == null) {
            Commons.showDialog(this, "Wait a moment!", "Please wait while this news is fully loaded!");
        } else {
            Gson gson = new Gson();
            String movieListJson = Pref.getPreferenceString(this, key);
            List<Movie> movieList;
            if (movieListJson == null || movieListJson.equals("")) {
                movieList = new ArrayList<>();
                Log.d("EXECUTED", "FUCK! " + movieListJson);
            } else {
                movieList = gson.fromJson(movieListJson, new TypeToken<List<Movie>>() {
                }.getType());
//                    Log.d("EXECUTED", newsListJson);
            }

//            this.movie.setDetailsResponse(this.movie.getDetailsResponse());
            movieList.add(this.movie);
            movieListJson = gson.toJson(movieList);
            Pref.savePreference(this, key, movieListJson);

            String message = null;
            if (key.equals(Pref.PREF_KEY_WISH_LIST))
                message = getResources().getString(R.string.addedToWishlistText);
            else if (key.equals(Pref.PREF_KEY_OFFLINE_LIST))
                message = getResources().getString(R.string.addedToOfflineText);
            Commons.showSimpleToast(this, message);
//                Log.d("CONVERTED_STRING", newsList.size() + "  " + newsListJson);
        }
    }


    private void updateViews(Movie movie) {
        this.movieNameTextView.setText(movie.getName());
        this.genereTextView.setText(movie.getGenere());
        this.durationTextView.setText(movie.getDuration());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        if (movie.getReleaseDate() != null)
            this.releaseDateTextView.setText(getResources().getString(R.string.releaseDateTextBangla) + ": " + sdf.format(movie.getReleaseDate()));
        this.storyLineTextView.setText(movie.getStoryLine());
        this.producerTextView.setText(getResources().getString(R.string.studioTextBangla) + " \n" + movie.getProductionHouse());
        this.filmRatingTextView.setText(String.valueOf(movie.getRated()));
        String imageUrl = getResources().getString(R.string.baseUrl) + "movie/image/" + movie.getUniqueId();
        Glide.with(this).load(imageUrl).placeholder(R.mipmap.ic_launcher).centerCrop().crossFade().into(this.posterImageView);
        this.setAverageRating(this.averageRatingBar, movie);
        this.movieIndustryTextView.setText("Movie Industry: " + movie.getIndustry());
        this.movieLanguageTextView.setText("Language: " + movie.getLanguage());
        Log.d("MOVIE_ID", movie.getUniqueId()+"");

//        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/SolaimanLipi.ttf");
//        this.movieNameTextView.setTypeface(typeface);
//        this.producerTextView.setTypeface(typeface);
//        this.filmRatingTextView.setTypeface(typeface);

        this.contentLayout.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);
    }

    private void setAverageRating(final RatingBar averageRatingBar, Movie movie) {
        final String url = getResources().getString(R.string.baseUrl) + "review/averagerating/movie/" + movie.getUniqueId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(DetailsActivity.this).fetchGetResponse(url);
                    final ResponseBody responseBody = response.body();
                    final String averageRating = responseBody.string();
                    responseBody.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_NO_CONTENT)
                                averageRatingBar.setRating(0f);
                            else if (response.code() == ResourceProvider.RESPONSE_CODE_OK)
                                averageRatingBar.setRating(Float.parseFloat(averageRating));
                        }
                    });
                } catch (IOException e) {
                    Log.e("AVERATE_RATING", e.toString());
                }
            }
        }).start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}
