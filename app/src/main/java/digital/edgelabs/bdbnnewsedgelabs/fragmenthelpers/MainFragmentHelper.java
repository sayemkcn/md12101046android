package digital.edgelabs.bdbnnewsedgelabs.fragmenthelpers;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.adapters.RecyclerAdapter;
import digital.edgelabs.bdbnnewsedgelabs.entity.CategoryEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsSourceEntity;
import digital.edgelabs.bdbnnewsedgelabs.events.UserCategoryLoadEvent;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;
import digital.edgelabs.bdbnnewsedgelabs.service.NewsProvider;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class MainFragmentHelper {
    private Activity context;
    private View rootView;
    private RecyclerView recyclerView;
    private static int PAGE_NUMBER = 0;

    private TextView textView;

    public MainFragmentHelper(Activity context, View rootView) {
        this.context = context;
        this.rootView = rootView;
        this.recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        // register eventbus
//        EventBus.getDefault().register(this);
    }

    public void exec(int pageNumber) {
        this.PAGE_NUMBER = pageNumber;

//        this.textView = (TextView) rootView.findViewById(R.id.textView);

        final String url = context.getResources().getString(R.string.baseUrl) + "/category/" + PAGE_NUMBER + ".json";
        Log.d("URL", url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        final String response = new NewsProvider(context).fetchNews(url);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onResponse(response, PAGE_NUMBER);
                            }
                        });
                    } catch (IOException e) {
                        Log.d("HTTP_EX", e.toString());
                    }
                }
            }
        }).start();
    }

    private void onResponse(String response, int categoryId) {
        try {
            CategoryEntity categoryEntity = this.parseJson(response);
            recyclerView.setAdapter(new RecyclerAdapter(context, categoryEntity));
            recyclerView.stopNestedScroll();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
//            textView.setText(categoryEntity.toString());
        } catch (JSONException e) {
            Log.d("JSON_EX", e.toString());
        }
    }

    private CategoryEntity parseJson(String response) throws JSONException {
        JSONObject catJsonObject = new JSONObject(response);
        CategoryEntity category = new CategoryEntity();
        category.setId(catJsonObject.getLong("categoryId"));
        category.setName(catJsonObject.getString("categoryName"));
        category.setAccentColorCode(catJsonObject.getString("accentColorCode"));
        category.setIconUrl(catJsonObject.getString("iconUrl"));

        List<NewsEntity> newsList = new ArrayList<>();

        JSONArray newsJsonArray = catJsonObject.getJSONArray("newsList");
        for (int i = 0; i < newsJsonArray.length(); i++) {
            JSONObject newsJsonObject = newsJsonArray.getJSONObject(i);
            NewsEntity news = new NewsEntity();
            news.setId(newsJsonObject.getLong("id"));
            news.setTitle(newsJsonObject.getString("title"));
            news.setDetails(newsJsonObject.getString("details"));
            news.setImageUrl(newsJsonObject.getString("imageUrl"));
            news.setAuthor(newsJsonObject.getString("author"));

            JSONObject sourceJsonObject = newsJsonObject.getJSONObject("source");
            NewsSourceEntity source = new NewsSourceEntity();
            source.setId(sourceJsonObject.getLong("sourceId"));
            source.setName(sourceJsonObject.getString("sourceName"));
            source.setIconUrl(sourceJsonObject.getString("iconUrl"));
            source.setAccentColorCode(sourceJsonObject.getString("sourceAccentColorCode"));

            news.setNewsSourceEntity(source);
            newsList.add(news);

        }

        category.setNewsEntityList(newsList);

        return category;
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onNewsFetched(UserCategoryLoadEvent newsFetchEvent) {
//            this.textView.setText(newsFetchEvent.getResponse());
//    }
}
