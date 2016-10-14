package digital.edgelabs.bdbnnewsedgelabs.fragmenthelpers;

import android.app.Activity;
import android.content.Intent;
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
            List<NewsEntity> newsList = this.parseJson(response);
            if (url.equals(context.getResources().getString(R.string.sliderNewsUrl)))
                this.updateSlider(newsList);
            else
                this.updateFeaturedNews(newsList);

        } catch (JSONException e) {
            Log.e("JSONEx", e.getMessage());
        } catch (ParseException e) {
            Log.e("ParseEx", e.getMessage());

        }
    }

    private void updateFeaturedNews(List<NewsEntity> newsList) {
        // WORK HERE
        this.featuredFeaturedRecyclerView.setAdapter(new FeaturedRecyclerAdapter(context,newsList));
        this.featuredFeaturedRecyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        this.featuredNewsLayout.setVisibility(View.VISIBLE);
    }


    private List<NewsEntity> parseJson(String response) throws JSONException, ParseException {
        List<NewsEntity> newsList = new ArrayList<>();

        JSONObject catJsonObject = new JSONObject(response);

        JSONArray newsJsonArray = catJsonObject.getJSONArray("newsList");
        for (int i = 0; i < newsJsonArray.length(); i++) {
            JSONObject newsJsonObject = newsJsonArray.getJSONObject(i);
            NewsEntity news = new NewsEntity();
            news.setId(newsJsonObject.getLong("id"));
            news.setTitle(newsJsonObject.getString("title"));

            String detailsNews = newsJsonObject.getString("details");
            if (detailsNews.length() > 75) {
                news.setDetails(detailsNews.substring(0, 75));
            } else {
                news.setDetails(detailsNews);
            }

            news.setImageUrl(newsJsonObject.getString("imageUrl"));
            news.setAuthor(newsJsonObject.getString("author"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            news.setLastUpdated(sdf.parse(newsJsonObject.getString("timestamp")));
//            news.setLastUpdated(sdf.parse("2016-08-31 20:01:41"));

//            Log.i("DATE",news.getLastUpdated().toString());

            JSONObject sourceJsonObject = newsJsonObject.getJSONObject("source");
            NewsSourceEntity source = new NewsSourceEntity();
            source.setId(sourceJsonObject.getLong("sourceId"));
            source.setName(sourceJsonObject.getString("sourceName"));
            source.setIconUrl(sourceJsonObject.getString("iconUrl"));
            source.setAccentColorCode(sourceJsonObject.getString("sourceAccentColorCode"));

            news.setNewsSourceEntity(source);
            newsList.add(news);

        }

        return newsList;
    }

    private void fetchOfflineAndBookmarkedNews() {
        List<NewsEntity> offlineNewsList = this.getOfflineNewsList();
        List<NewsEntity> bookmarkedNewsList = this.getBookmarkedNewsList();
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

    private void setUpRecyclerView(RecyclerView recyclerView, List<NewsEntity> newsList) {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(new RecyclerAdapter(this.context, newsList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.context));
//        this.recyclerView.setVisibility(View.VISIBLE);
    }

    private List<NewsEntity> getOfflineNewsList() {
        String newsListJson = Pref.getPreferenceString(context, Pref.PREF_KEY_OFFLINE_NEWS_LIST);
        if (newsListJson != null && !newsListJson.equals("")) {
            Gson gson = new Gson();
            return gson.fromJson(newsListJson, new TypeToken<List<NewsEntity>>() {
            }.getType());
        }
        return null;
    }


    private List<NewsEntity> getBookmarkedNewsList() {
        String newsListJson = Pref.getPreferenceString(context, Pref.PREF_KEY_BOOKMARK_LIST);
        if (newsListJson != null && !newsListJson.equals("")) {
            Gson gson = new Gson();
            return gson.fromJson(newsListJson, new TypeToken<List<NewsEntity>>() {
            }.getType());
        }
        return null;
    }

    // set json data to view
    public void updateSlider(final List<NewsEntity> newsList) {
        if (context != null) {
            for (final NewsEntity item : newsList) {
                String iconUrl = item.getNewsSourceEntity().getIconUrl();
                CustomSliderView textSliderView = new CustomSliderView(context, iconUrl);
                // initialize a SliderLayout
                textSliderView
                        .description(item.getTitle())
                        .image(item.getImageUrl())
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                            @Override
                            public void onSliderClick(BaseSliderView slider) {
                                context.startActivity(new Intent(context, DetailsActivity.class).putExtra("newsId", item.getId())
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        });

//                ImageView iconView = (ImageView) textSliderView.getView().findViewById(R.id.sliderNewsSourceIcon);
//                Glide.with(context).load(item.getNewsSourceEntity().getIconUrl()).placeholder(R.mipmap.ic_launcher).into(iconView);
                this.sliderLayout.addSlider(textSliderView);
            }

        }
    }
}
