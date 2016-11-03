package net.toracode.moviedb;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import net.toracode.moviedb.commons.Pref;
import net.toracode.moviedb.entity.CustomList;
import net.toracode.moviedb.entity.Movie;
import net.toracode.moviedb.fragmants.ReviewFragment;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

public class DetailsActivity extends AppCompatActivity {
    @BindView(R.id.detailsImageView)
    ImageView detailsImageView;
    @BindView(R.id.movieNameTextView)
    TextView movieNameTextView;
    @BindView(R.id.movieTypeTextView)
    TextView movieTypeTextView;
    @BindView(R.id.directorNameTextView)
    TextView directorNameTextView;
    @BindView(R.id.producerNameTextView)
    TextView producerTextView;
    @BindView(R.id.ratingTextView)
    TextView ratingTextView;

    @BindView(R.id.contentLayout)
    View contentLayout;
    @BindView(R.id.detailsProgressBar)
    ProgressBar progressBar;
    @BindView(R.id.reviewFragmentContainer)
    View reviewFragmentContainer;

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
//        getSupportActionBar().hide();

        // register eventbus
        ButterKnife.bind(this);

        this.typeface = typeface.createFromAsset(getAssets(), "fonts/SolaimanLipi.ttf");

        this.movie = (Movie) getIntent().getExtras().getSerializable("movie");

        this.updateViews(this.movie);
//        this.loadNewsFromServer(movie);
        Log.i("MOVIE", movie.toString());
        getSupportFragmentManager().beginTransaction().replace(R.id.reviewFragmentContainer, ReviewFragment.newInstance(movie.getUniqueId())).commit();

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
                    final String responseBody = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (loadingToast.isShowing()) loadingToast.dismiss();
                            if (response.code() == ResourceProvider.RESPONSE_CODE_BAD_REQUEST) {
                                Commons.showSimpleToast(getApplicationContext(), "You\'re not logged in. Please login to continue.");
                                startActivity(new Intent(DetailsActivity.this, PreferenceActivity.class));
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_NOT_FOUND) { // if list is empty for the user.
                                createCustomList("Watchlist", "This is your private watchlist. This list auto generated for you.", "private");
                                List<CustomList> listOfCustomList = parseCustomList(responseBody);
                                showAddToListDialog(listOfCustomList);
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_FOUND) { // if list is not empty then show a chooser dialog
                                List<CustomList> listOfCustomList = parseCustomList(responseBody);
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
                .negativeText("New List")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showNewListDialog();
                    }
                })
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

    // create a custom list dialog.
    private void showNewListDialog() {
        new MaterialDialog.Builder(this)
                .title("Create new list")
                .customView(R.layout.create_custom_list, true)
                .positiveText("Create")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        EditText nameEditText = (EditText) view.findViewById(R.id.listName);
                        EditText descEditText = (EditText) view.findViewById(R.id.listDescription);
                        EditText typeEditText = (EditText) view.findViewById(R.id.listType);
                        String name = nameEditText.getText().toString();
                        String desc = descEditText.getText().toString();
                        String type = typeEditText.getText().toString();
//
                        createCustomList(name, desc, type);
                    }
                })
                .show();

    }

    private void createCustomList(final String name, String desc, String type) {
        if (AccountKit.getCurrentAccessToken() == null) {
            this.startActivity(new Intent(this, PreferenceActivity.class));
            return;
        }

        String accountId = AccountKit.getCurrentAccessToken().getAccountId();
        final String url = getResources().getString(R.string.baseUrl) + "list/create?accountId=" + accountId + "&title=" + name + "&description=" + desc + "&type=" + type;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(DetailsActivity.this).fetchPostResponse(url);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_INTERNAL_SERVER_ERROR) {
                                Commons.showSimpleToast(getApplicationContext(), "Can not create list!");
                            } else if (response.code() == ResourceProvider.RESPONSE_NOT_ACCEPTABLE) {
                                Commons.showDialog(DetailsActivity.this, "Can not create list!", "1. You must enter a name (length should be at least three letters)\n" +
                                        "2. You must enter a type. Type can be anything you want but if it's \"public\" the list will be shown to all.");
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_CREATED) {
                                String message;
                                if (name.toLowerCase().equals("watchlist")) {
                                    message = "We have created an watchlist for you. You can create new list of your own by clicking new list button on the dialog.";
                                } else {
                                    message = "Your list has been created successfully.";
                                }
                                Commons.showDialog(DetailsActivity.this, "Successfull!", message);
                            }
                        }
                    });

                } catch (IOException e) {
                    Log.e("CREATE_CUSTOM_LIST", e.toString());
                }
            }
        }).start();

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
        this.movieTypeTextView.setText(getResources().getString(R.string.categoryTextBangla) + " \n" + movie.getIndustry());
        this.directorNameTextView.setText(getResources().getString(R.string.directorTextBangla) + " \n" + movie.getProductionHouse());
        this.producerTextView.setText(getResources().getString(R.string.producerTextBangla) + " \n" + movie.getProductionHouse());
        this.ratingTextView.setText(String.valueOf(movie.getRated()));
//        this.createImageView(movie.getThumbnailUrls());

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/SolaimanLipi.ttf");
        this.movieNameTextView.setTypeface(typeface);
        this.movieTypeTextView.setTypeface(typeface);
        this.directorNameTextView.setTypeface(typeface);
        this.producerTextView.setTypeface(typeface);
        this.ratingTextView.setTypeface(typeface);

        this.contentLayout.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);
    }

    private void createImageView(String[] imageUrls) {
        GridLayout layout = (GridLayout) findViewById(R.id.thumbnailView);
        for (int i = 0; i < imageUrls.length; i++) {
            ImageView image = new ImageView(this);
            image.setLayoutParams(new android.view.ViewGroup.LayoutParams(200, 200));
            image.setMaxHeight(200);
            image.setMaxWidth(200);
            Glide.with(this).load(imageUrls[i]).crossFade().into(image);

            // Adds the view to the layout
            layout.addView(image);
        }
    }

}
