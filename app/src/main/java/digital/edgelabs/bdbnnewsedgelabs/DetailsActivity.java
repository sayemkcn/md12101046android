package digital.edgelabs.bdbnnewsedgelabs;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.entity.Movie;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;
import digital.edgelabs.bdbnnewsedgelabs.service.NewsProvider;

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

    private Movie movie;

    private Typeface typeface;

    int count = 0;

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

        this.loadNewsFromServer(movie);

    }

    private void showOfflineMovieItem(Movie movie) {
        if (this.progressBar.getVisibility() == View.VISIBLE) {
            this.contentLayout.setVisibility(View.VISIBLE);
            this.progressBar.setVisibility(View.GONE);
        }
        this.onResponse(movie.getDetailsResponse());
    }

    private void loadNewsFromServer(final Movie movie) {
        Log.d("NEWS_DETAILS_URL", movie.getDetailsUrl());
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        final String response = new NewsProvider(DetailsActivity.this).fetchNews(getResources().getString(R.string.baseUrl) + movie.getDetailsUrl());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onResponse(response);
                            }
                        });
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Commons.showDialog(DetailsActivity.this, "Connection unavailable!", "Looks like your internet connection is too slow or there\'s no internet at all! Please connect to the internet first!");
                            }
                        });
                    }
                }
            }
        }).start();
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
        } else if (id == R.id.action_bookmark) {
//            this.createBookmark();
        } else if (id == R.id.action_save) {
            this.saveOffline();
        } else if (id == R.id.action_share) {
            Commons.share(this, "Share this news", getResources().getString(R.string.singleNewsBaseUrl) + this.movie.getId());
        }
        return super.onOptionsItemSelected(item);
    }


    private void saveOffline() {
        if (this.movie == null) {
            Commons.showDialog(this, "Wait a moment!", "Please wait while this news is fully loaded!");
        } else {
            Gson gson = new Gson();
            String movieListJson = Pref.getPreferenceString(this, Pref.PREF_KEY_OFFLINE_NEWS_LIST);
            List<Movie> movieList;
            if (movieListJson == null || movieListJson.equals("")) {
                movieList = new ArrayList<>();
                Log.d("EXECUTED", "FUCK! " + movieListJson);
            } else {
                movieList = gson.fromJson(movieListJson, new TypeToken<List<Movie>>() {
                }.getType());
//                    Log.d("EXECUTED", newsListJson);
            }

            this.movie.setDetailsResponse(this.movie.getDetailsResponse());
            movieList.add(this.movie);
            movieListJson = gson.toJson(movieList);
            Pref.savePreference(this, Pref.PREF_KEY_OFFLINE_NEWS_LIST, movieListJson);
            Commons.showSimpleToast(this, "Item saved for offline reading..");
//                Log.d("CONVERTED_STRING", newsList.size() + "  " + newsListJson);
        }
    }

//    private void createBookmark() {
//        if (this.movie == null) {
//            Commons.showDialog(this, "Wait a moment!", "Please wait while this news is fully loaded!");
//        } else {
//            Gson gson = new Gson();
//            String newsListJson = Pref.getPreferenceString(this, Pref.PREF_KEY_BOOKMARK_LIST);
//            List<NewsEntity> newsList;
//            if (newsListJson == null || newsListJson.equals("")) {
//                newsList = new ArrayList<>();
//                Log.d("EXECUTED", "FUCK! " + newsListJson);
//            } else {
//                newsList = gson.fromJson(newsListJson, new TypeToken<List<NewsEntity>>() {
//                }.getType());
////                    Log.d("EXECUTED", newsListJson);
//            }
//
//            this.news.setDetails(this.news.getDetails().substring(0, 100) + "..");
//            newsList.add(this.news);
//            newsListJson = gson.toJson(newsList);
//            Pref.savePreference(this, Pref.PREF_KEY_BOOKMARK_LIST, newsListJson);
//            Commons.showSimpleToast(this, "Bookmark Added!");
////                Log.d("CONVERTED_STRING", newsList.size() + "  " + newsListJson);
//        }
//    }


    private void onResponse(String response) {
        try {
            if (this.progressBar.getVisibility() == View.VISIBLE) {
                this.contentLayout.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.GONE);
            }
            Glide.with(this).load(this.parseImageUrl(response)).centerCrop().placeholder(R.mipmap.ic_launcher).diskCacheStrategy(DiskCacheStrategy.ALL).into(this.detailsImageView);
            this.parseData(response);

        } catch (JSONException e) {
            Commons.showDialog(this, "Connection unavailable!", "Looks like your internet connection is too slow or there\'s no internet connection at all! Please connect to the internet first!");
        } catch (ParseException e) {
            Log.d("PARSE_EX", e.toString());
        }
    }


    private String parseImageUrl(String response) {
        return Jsoup.parse(response).getElementsByClass("main-movie-poster").get(0).getElementsByTag("img").get(0).attr("src");
    }


    private void parseData(String response) throws JSONException, ParseException {
        try {
            /// PARSE DATA AND UPDATE VIEW
            Elements elements = Jsoup.parseBodyFragment(response).body().getElementsByClass("home-box").get(0).getElementsByTag("ul").get(0).getElementsByTag("li");
            Elements links = elements.get(0).getElementsByTag("a");
            StringBuilder categoryBuilder = new StringBuilder();
            for (int i = 0; i < links.size(); i++) {
                categoryBuilder.append(links.get(i).text());
                int index = i + 1;
                if (index != links.size())
                    categoryBuilder.append(",");
            }
            String producer = elements.get(2).getElementsByTag("a").get(0).text();
            Elements thumnailElements = Jsoup.parseBodyFragment(response).body().getElementsByClass("thumbnails").get(0).getElementsByTag("img");
            String[] thumbnailUrls = new String[thumnailElements.size()];
            for (int i = 0; i < thumnailElements.size(); i++) {
                thumbnailUrls[i] = thumnailElements.get(i).attr("src");
            }

            this.movie.setCategory(categoryBuilder.toString());
            this.movie.setProducerName(producer);
            this.movie.setThumbnailUrls(thumbnailUrls);

            this.updateViews(this.movie);
        } catch (Exception e) {
            Log.e("JSOUP_PARSE", e.getMessage());
        }
    }

    private void updateViews(Movie movie) {
        this.movieNameTextView.setText(movie.getName());
        this.movieTypeTextView.setText(getResources().getString(R.string.categoryTextBangla) + " \n" + movie.getCategory());
        this.directorNameTextView.setText(getResources().getString(R.string.directorTextBangla) + " \n" + movie.getDirectorName());
        this.producerTextView.setText(getResources().getString(R.string.producerTextBangla) + " \n" + movie.getProducerName());
        this.ratingTextView.setText(movie.getRating());
        this.createImageView(movie.getThumbnailUrls());

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
