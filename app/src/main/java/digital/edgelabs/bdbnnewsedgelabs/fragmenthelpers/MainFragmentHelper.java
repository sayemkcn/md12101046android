package digital.edgelabs.bdbnnewsedgelabs.fragmenthelpers;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.entity.CategoryEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsSourceEntity;
import digital.edgelabs.bdbnnewsedgelabs.service.NewsProvider;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class MainFragmentHelper {
    private Activity context;
    private View rootView;


    private static int PAGE_NUMBER = 0;

    private TextView textView;

    public MainFragmentHelper(Activity context, View rootView) {
        this.context = context;
        this.rootView = rootView;

        // register eventbus
//        EventBus.getDefault().register(this);
    }

    public void exec(int pageNumber) {
        this.PAGE_NUMBER = pageNumber;

        this.textView = (TextView) rootView.findViewById(R.id.textView);

        final String url = context.getResources().getString(R.string.baseUrl) + "/category/" + PAGE_NUMBER + ".json";
        Log.d("URL", url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    try {
                        final String response = new NewsProvider(context).fetchNews(url);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onResponse(response,PAGE_NUMBER);
                            }
                        });
                    } catch (IOException e) {
                        Log.d("HTTP_EX",e.toString());
                    }
                }
            }
        }).start();


    }

    private void onResponse(String response,int categoryId){
        try{
            CategoryEntity categoryEntity = this.parseJson(response);
            textView.setText(categoryEntity.toString());
        }catch (JSONException e){
            Log.d("JSON_EX",e.toString());
        }
    }

    private CategoryEntity parseJson(String response) throws JSONException {
        JSONObject catJsonObject = new JSONObject(response);
        CategoryEntity category = new CategoryEntity();
        category.setId(catJsonObject.getLong("categoryId"));
        category.setName(catJsonObject.getString("categoryName"));
        category.setAccentColorCode(catJsonObject.getString("accentColorCode"));
        category.setIconUrl(catJsonObject.getString("iconUrl"));

        List<NewsSourceEntity> newsSourceList = new ArrayList<>();

        JSONArray sourceJsonArray = catJsonObject.getJSONArray("sourceList");
        for (int i=0;i<sourceJsonArray.length();i++){
            JSONObject sourceJsonObject = sourceJsonArray.getJSONObject(i);
            NewsSourceEntity newsSource = new NewsSourceEntity();
            newsSource.setId(sourceJsonObject.getLong("sourceId"));
            newsSource.setName(sourceJsonObject.getString("sourceName"));
            newsSource.setIconUrl(sourceJsonObject.getString("iconUrl"));

            // nested parsing for retriving news list
            JSONArray newsJsonArray = sourceJsonObject.getJSONArray("newsList");
            List<NewsEntity> newsList = new ArrayList<>();
            for (int j=0;j<newsJsonArray.length();j++){
                JSONObject newsJsonObject = newsJsonArray.getJSONObject(j);
                NewsEntity news = new NewsEntity();
                news.setId(newsJsonObject.getLong("id"));
                news.setSourceId(newsJsonObject.getLong("sourceId"));
                news.setTitle(newsJsonObject.getString("title"));
                news.setDetails(newsJsonObject.getString("details"));
                news.setImageUrl(newsJsonObject.getString("imageUrl"));
                news.setAuthor(newsJsonObject.getString("author"));
//                news.setLastUpdated(); // IT WILL BE FIXED LATER
                newsList.add(news);
            }
            newsSource.setNewsList(newsList);
            newsSourceList.add(newsSource);
        }

        category.setNewsSourceList(newsSourceList);

        return category;
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onNewsFetched(NewsFetchEvent newsFetchEvent) {
//            this.textView.setText(newsFetchEvent.getResponse());
//    }
}
