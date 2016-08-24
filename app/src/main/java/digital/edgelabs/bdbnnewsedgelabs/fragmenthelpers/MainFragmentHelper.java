package digital.edgelabs.bdbnnewsedgelabs.fragmenthelpers;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import digital.edgelabs.bdbnnewsedgelabs.Commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.adapters.RecyclerAdapter;
import digital.edgelabs.bdbnnewsedgelabs.entity.CategoryEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsSourceEntity;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;
import digital.edgelabs.bdbnnewsedgelabs.service.NewsProvider;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class MainFragmentHelper {
    private Activity context;
    private RecyclerView recyclerView;
    private static int PAGE_NUMBER = 0;
    private ProgressBar progressBar;

    public MainFragmentHelper(Activity context, View rootView) {
        this.context = context;
        this.recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        this.progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        // register eventbus
//        EventBus.getDefault().register(this);
    }

    public void exec(int pageNumber) {
        this.PAGE_NUMBER = pageNumber;

        // Build request url with filter
        // get news source url from sharedpref and send them as param
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder
                .append(context.getResources().getString(R.string.baseUrl))
                .append("/category/")
                .append(PAGE_NUMBER)
                .append(".json")
                .append("?sources=");
        for (int i = 0; i < Pref.getPreferenceInt(context, Pref.PREF_SIZE); i++) {
            if (Pref.getPreference(context, "source" + (i + 1))) {
                urlBuilder.append((i + 1) + ",");
            }
        }
//        Log.i("url",urlBuilder.toString());
        String tempUrl = this.concatAllSourceIdIfNone(urlBuilder.toString(),context.getResources().getStringArray(R.array.sourceNames).length);
        final String url = tempUrl.replaceAll(",$", "");
        Log.d("URL", url);
        Toast.makeText(context, url, Toast.LENGTH_LONG).show();

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
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Commons.showDialog(context, "Connection unavailable!", "Looks like your internet connection is too slow or there\'s no internet at all! Please connect to the internet first!");
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private String concatAllSourceIdIfNone(String url, int prefSize) {
        if (url.split("=").length<2){
            for (int i=0;i<prefSize;i++){
                url+=i+1+",";
            }
        }
        return url;
    }

    private void onResponse(String response, int categoryId) {
        try {
            CategoryEntity categoryEntity = this.parseJson(response);
            recyclerView.setAdapter(new RecyclerAdapter(context, categoryEntity));
            recyclerView.stopNestedScroll();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setVisibility(View.VISIBLE);
            this.progressBar.setVisibility(View.GONE);

//            textView.setText(categoryEntity.toString());
        } catch (JSONException e) {
            Log.d("JSON_EX", e.toString());
        } catch (ParseException e) {
            Log.e("PARSE_DATE_SDF", e.toString());
        }
    }

    private CategoryEntity parseJson(String response) throws JSONException, ParseException {
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            news.setLastUpdated(sdf.parse(newsJsonObject.getString("timestamp")));
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

        category.setNewsEntityList(newsList);

        return category;
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onNewsFetched(UserCategoryLoadEvent newsFetchEvent) {
//            this.textView.setText(newsFetchEvent.getResponse());
//    }
}
