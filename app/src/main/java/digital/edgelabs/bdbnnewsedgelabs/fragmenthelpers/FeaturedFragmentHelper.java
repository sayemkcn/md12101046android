package digital.edgelabs.bdbnnewsedgelabs.fragmenthelpers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.DetailsActivity;
import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.adapters.FeaturedRecyclerAdapter;
import digital.edgelabs.bdbnnewsedgelabs.adapters.RecyclerAdapter;
import digital.edgelabs.bdbnnewsedgelabs.commons.CustomSliderView;
import digital.edgelabs.bdbnnewsedgelabs.commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.commons.SliderChildAnimator;
import digital.edgelabs.bdbnnewsedgelabs.entity.Movie;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsSourceEntity;
import digital.edgelabs.bdbnnewsedgelabs.service.NewsProvider;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class FeaturedFragmentHelper {
    private Activity context;
    private static int VP_PAGE_NUMBER = 0;
    private RecyclerView featuredOfflineRecyclerView;
    private RecyclerView featuredBookmarksRecyclerView;
    private RecyclerView featuredFeaturedRecyclerView;
    private TextView noItemsTextOffline;
    private TextView noItemsTextBookmarks;
    private SliderLayout sliderLayout;

    private View featuredNewsLayout;
//    private ProgressBar progressBar;


    public FeaturedFragmentHelper(Activity context, View rootView) {
        this.context = context;
        ButterKnife.bind(context);
        this.featuredOfflineRecyclerView = (RecyclerView) rootView.findViewById(R.id.featuredOfflineRecyclerView);
        this.featuredBookmarksRecyclerView = (RecyclerView) rootView.findViewById(R.id.featuredBookmarkedRecyclerView);
        this.featuredFeaturedRecyclerView = (RecyclerView) rootView.findViewById(R.id.featuredFeaturedRecyclerView);
        this.noItemsTextOffline = (TextView) rootView.findViewById(R.id.offline_no_items_text);
        this.noItemsTextBookmarks = (TextView) rootView.findViewById(R.id.bookmarks_no_items_text);
        this.sliderLayout = (SliderLayout) rootView.findViewById(R.id.sliderLayout);
        // Slider Layout
        this.sliderLayout = (SliderLayout) rootView.findViewById(R.id.sliderLayout);
        this.sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
        this.sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        this.sliderLayout.setCustomAnimation(new SliderChildAnimator());
        this.sliderLayout.setDuration(4000);

        this.featuredNewsLayout = rootView.findViewById(R.id.featuredNewsLayout);

        // set typeface to title textviews
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
        TextView featuredTitleTextView = (TextView) rootView.findViewById(R.id.featuredTitleTextView);
        TextView savedTitleTextView = (TextView)rootView.findViewById(R.id.savedTitleText);
        TextView offlineTitleTextView = (TextView)rootView.findViewById(R.id.offlineTitleText);
        featuredTitleTextView.setTypeface(typeface);
        savedTitleTextView.setTypeface(typeface);
        offlineTitleTextView.setTypeface(typeface);
    }

    public void exec(int pageNumber) {
        this.VP_PAGE_NUMBER = pageNumber;
        Log.d("SECTION_NUMBER", String.valueOf(pageNumber));

        this.fetchOfflineAndBookmarkedNews();
        this.fetchSliderNews();
        this.fetchFeaturedNews();

    }

    private void fetchFeaturedNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = context.getResources().getString(R.string.featuredNewsUrl);
                try {
                    final String response = new NewsProvider(context).fetchNews(url);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onResponse(url, response);
                        }
                    });
                } catch (IOException e) {
                    Log.d("IOExFeatured", e.getMessage());
                }
            }
        }).start();
    }

    private void fetchSliderNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = context.getResources().getString(R.string.sliderNewsUrl);
                try {
                    final String response = new NewsProvider(context).fetchNews(url);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onResponse(url, response);
                        }
                    });
                } catch (IOException e) {
                    Log.e("IOExSlider", e.getMessage());
                }
            }
        }).start();

    }

    private void onResponse(String url, String response) {
        try {
            List<Movie> movieList = this.parseJson(response);
            if (url.equals(context.getResources().getString(R.string.sliderNewsUrl)))
                this.updateSlider(movieList);
            else
                this.updateFeaturedNews(movieList);

        } catch (JSONException e) {
            Log.e("JSONEx", e.getMessage());
        } catch (ParseException e) {
            Log.e("ParseEx", e.getMessage());

        }
    }

    private void updateFeaturedNews(List<Movie> movieList) {
        // WORK HERE
        this.featuredFeaturedRecyclerView.setAdapter(new FeaturedRecyclerAdapter(context,movieList));
        this.featuredFeaturedRecyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        this.featuredNewsLayout.setVisibility(View.VISIBLE);
        this.featuredFeaturedRecyclerView.setNestedScrollingEnabled(false);
    }


    private List<Movie> parseJson(String response) throws JSONException, ParseException {
        List<Movie> movieList = new ArrayList<>();
        try {
            // FILL UP MOVIE LIST
            Document document = Jsoup.parseBodyFragment(response);
            Element body = document.body();
            Elements homeBoxes = body.getElementsByClass("home-box");
            // for every posts
            for (int i = 0; i < homeBoxes.size(); i++) {
                if (i != 0) {
                    Movie movie = new Movie();
                    String imgUrl = homeBoxes.get(i).getElementsByTag("img").get(0).attr("src");
                    String name = homeBoxes.get(i).getElementsByTag("h3").get(0).getElementsByTag("a").text();
                    String detailsUrl = homeBoxes.get(i).getElementsByTag("h3").get(0).getElementsByTag("a").attr("href");
                    Elements metaElements = homeBoxes.get(i).getElementsByTag("ul").get(0).getElementsByTag("li");
                    String directorName = null;
                    Elements castsElements = null;
                    String releaseDate = null;
                    String rating = null;
                    if (metaElements.size() == 2) {
                        directorName = metaElements.get(0).getElementsByTag("a").text();
                        castsElements = metaElements.get(1).getElementsByTag("a");
                    } else if (metaElements.size() == 3) {
                        castsElements = metaElements.get(1).getElementsByTag("a");
                        releaseDate = metaElements.get(0).text();
                        rating = metaElements.get(2).text();
                    } else if (metaElements.size() == 4) {
                        directorName = metaElements.get(1).getElementsByTag("a").text();
                        castsElements = metaElements.get(2).getElementsByTag("a");
                        releaseDate = metaElements.get(0).text();
                        rating = metaElements.get(3).text();
                    }
                    String[] casts = new String[castsElements.size()];
                    for (int j = 0; j < castsElements.size() && j < casts.length; j++)
                        casts[j] = castsElements.get(j).text();


                    movie.setImageUrl(imgUrl);
                    movie.setName(name);
                    movie.setDetailsUrl(detailsUrl);
                    movie.setDirectorName(directorName);
                    movie.setCasts(casts);
                    movie.setReleaseDate(releaseDate);
                    movie.setRating(rating);

                    movieList.add(movie);
                }
            }
        } catch (Exception e) {
            Log.e("CAN_NOT_PARSE", e.getMessage());
        }
        return movieList;
    }

    private void fetchOfflineAndBookmarkedNews() {
        List<Movie> offlineNewsList = this.getOfflineNewsList();
        List<Movie> bookmarkedNewsList = this.getBookmarkedNewsList();
        // load offline news
        if (offlineNewsList == null || offlineNewsList.isEmpty()) {
            this.noItemsTextOffline.setVisibility(View.VISIBLE);
        } else {
            // if list not empty
            Collections.reverse(offlineNewsList);
            // Limit 3 news items
            if (offlineNewsList.size() > 3) {
                this.setUpRecyclerView(this.featuredOfflineRecyclerView, offlineNewsList.subList(0, 3));
            } else {
                this.setUpRecyclerView(this.featuredOfflineRecyclerView, offlineNewsList);
            }
        }
        // load Bookmarks
        if (bookmarkedNewsList == null || bookmarkedNewsList.isEmpty()) {
            this.noItemsTextBookmarks.setVisibility(View.VISIBLE);
        } else {
            Collections.reverse(bookmarkedNewsList);
            // Limit 3 news Items
            if (bookmarkedNewsList.size() > 3) {
                this.setUpRecyclerView(this.featuredBookmarksRecyclerView, bookmarkedNewsList.subList(0, 3));
            } else {
                this.setUpRecyclerView(this.featuredBookmarksRecyclerView, bookmarkedNewsList);
            }
        }

    }

    private void setUpRecyclerView(RecyclerView recyclerView, List<Movie> movieList) {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(new RecyclerAdapter(this.context, movieList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.context));
//        this.recyclerView.setVisibility(View.VISIBLE);
    }

    private List<Movie> getOfflineNewsList() {
        String newsListJson = Pref.getPreferenceString(context, Pref.PREF_KEY_OFFLINE_LIST);
        if (newsListJson != null && !newsListJson.equals("")) {
            Gson gson = new Gson();
            return gson.fromJson(newsListJson, new TypeToken<List<Movie>>() {
            }.getType());
        }
        return null;
    }


    private List<Movie> getBookmarkedNewsList() {
        String newsListJson = Pref.getPreferenceString(context, Pref.PREF_KEY_WISH_LIST);
        if (newsListJson != null && !newsListJson.equals("")) {
            Gson gson = new Gson();
            return gson.fromJson(newsListJson, new TypeToken<List<Movie>>() {
            }.getType());
        }
        return null;
    }

    // set json data to view
    public void updateSlider(final List<Movie> newsList) {
        if (context != null) {
            for (final Movie item : newsList) {
                String iconUrl = item.getImageUrl();
                CustomSliderView textSliderView = new CustomSliderView(context, iconUrl);
                // initialize a SliderLayout
                textSliderView
                        .description(item.getName())
                        .image(item.getImageUrl())
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                            @Override
                            public void onSliderClick(BaseSliderView slider) {
                                context.startActivity(new Intent(context, DetailsActivity.class).putExtra("movie", item)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        });

//                ImageView iconView = (ImageView) textSliderView.getView().findViewById(R.id.sliderNewsSourceIcon);
//                Glide.with(context).load(item.getNewsSourceEntity().getIconUrl()).placeholder(R.mipmap.ic_launcher).into(iconView);
                this.sliderLayout.addSlider(textSliderView);
            }

        }

        // set visibility
        this.sliderLayout.setVisibility(View.VISIBLE);
    }
}
