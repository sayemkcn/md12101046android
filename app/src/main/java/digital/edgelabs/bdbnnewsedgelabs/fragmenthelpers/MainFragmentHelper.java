package digital.edgelabs.bdbnnewsedgelabs.fragmenthelpers;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.github.johnpersano.supertoasts.library.SuperToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.adapters.RecyclerAdapter;
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
    private static int VP_PAGE_NUMBER = 1;
    private ProgressBar progressBar;

    private Button moreButton;

    private int startIndex = 1;
    private int pageSize = 10;
    private List<NewsEntity> newsList = new ArrayList<>();
    private SuperToast toast;

    String[] categories;

    public MainFragmentHelper(Activity context, View rootView) {
        this.context = context;
        ButterKnife.bind(context);
        this.toast = Commons.getLoadingToast(context);
        this.recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        this.progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        this.moreButton = (Button) rootView.findViewById(R.id.moreButton);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
        this.moreButton.setTypeface(typeface);
        this.categories = context.getResources().getStringArray(R.array.categories);
        // register eventbus
//        EventBus.getDefault().register(this);
    }

    public void exec(int pageNumber) {
        this.VP_PAGE_NUMBER = pageNumber;
//        Log.d("URL", url);
//        Toast.makeText(context, url, Toast.LENGTH_LONG).show();

        this.fetchNews(VP_PAGE_NUMBER, this.startIndex, this.pageSize);

    }

    private void fetchNews(final int vpPageNumber, final int startIndex, final int pageSize) {
        this.toast.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        final String url = buildUrl(context, vpPageNumber, startIndex, pageSize);
                        Log.i("URL: ", url);
                        final String response = new NewsProvider(context).fetchNews(url);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onResponse(response, vpPageNumber);
                                if (toast.isShowing()) toast.dismiss();
                            }
                        });
                    } catch (IOException e) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (toast.isShowing()) toast.dismiss();
                                Commons.showNetworkUnavailableDialog(context, "Connection unavailable!",
                                        "Looks like your internet connection is too slow or there\'s no internet at all! Please connect to the internet first!\nWant to read your saved news items?");
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private String buildUrl(Activity context, int vpPageNumber, int startIndex, int pageSize) {
        // Build request url with filter
        // get news source url from sharedpref and send them as param
        String[] newsSourceParamValues = context.getResources().getStringArray(R.array.newsSourceParamValues);
        int prefSize = Pref.getPreferenceInt(context, Pref.PREF_SIZE);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder
                .append(context.getResources().getString(R.string.baseUrl))
                .append("/category/")
                .append(categories[vpPageNumber-1])
//                .append(".json")
                .append("?sources=");
        for (int i = 0; i < prefSize; i++) {
            if (Pref.getPreference(context, "source" + (i + 1))) {
                urlBuilder.append(newsSourceParamValues[i] + ",");
//                Log.d("DOURCE",this.newsSourceParamValues[i]);
            }
        }
        String tempUrl = this.concatAllSourceIdIfNone(urlBuilder.toString(), newsSourceParamValues);
        return tempUrl.replaceAll(",$", "") + "&start=" + startIndex + "&size=" + pageSize;
    }

    private String concatAllSourceIdIfNone(String url, String[] newsSourceParamValues) {
        if (url.split("=").length < 2) {
            // concat source id if no source is set yet
//            for (int i = 0; i < newsSourceParamValues.length; i++) {
//                url += i + 1 + ",";
//            }
            // concat source name if no source is set yet
            for (int i = 0; i < newsSourceParamValues.length; i++) {
                url += newsSourceParamValues[i] + ",";
            }
        }
        return url;
    }

    private void onResponse(String response, final int vpPageNumber) {
        try {
            this.newsList = this.parseJson(response);
//            if (this.newsList.size()>pageSize){
//                RecyclerAdapter adapter = new RecyclerAdapter(context,this.newsList);
//                adapter.notifyDataSetChanged();
//                recyclerView.setAdapter(adapter);
//            }else {
            this.setUpRecyclerView(context, newsList, vpPageNumber);
//            }

            this.progressBar.setVisibility(View.GONE);
            this.moreButton.setVisibility(View.VISIBLE);
            this.moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startIndex += pageSize;
                    fetchNews(vpPageNumber, startIndex, pageSize);
                }
            });
//            textView.setText(categoryEntity.toString());
        } catch (JSONException e) {
            Log.d("JSON_EX", e.toString());
        } catch (ParseException e) {
            Log.e("PARSE_DATE_SDF", e.toString());
        }
    }


    private void setUpRecyclerView(final Activity context, List<NewsEntity> newsList, final int vpPageNumber) {
        final RecyclerAdapter adapter = new RecyclerAdapter(context, this.newsList);
        if (this.newsList.size() > pageSize) {
            adapter.notifyDataSetChanged();
        }
        recyclerView.setAdapter(adapter);
        recyclerView.stopNestedScroll();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private List<NewsEntity> parseJson(String response) throws JSONException, ParseException {
        JSONObject catJsonObject = new JSONObject(response);

        JSONArray newsJsonArray = catJsonObject.getJSONArray("newsList");
        for (int i = 0; i < newsJsonArray.length(); i++) {
            JSONObject newsJsonObject = newsJsonArray.getJSONObject(i);
            NewsEntity news = new NewsEntity();
            news.setId(newsJsonObject.getLong("id"));
            news.setTitle(newsJsonObject.getString("title"));

            String detailsNews = newsJsonObject.getString("details");
            if (detailsNews.length()>100){
                news.setDetails(detailsNews.substring(0, 100));
            }else {
                news.setDetails(detailsNews);
            }

            news.setImageUrl(newsJsonObject.getString("imageUrl"));
            news.setAuthor(newsJsonObject.getString("author"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            news.setLastUpdated(sdf.parse(newsJsonObject.getString("timestamp")));
            news.setLastUpdated(sdf.parse("2016-08-31 20:01:41"));

//            Log.i("DATE",news.getLastUpdated().toString());

            JSONObject sourceJsonObject = newsJsonObject.getJSONObject("source");
            NewsSourceEntity source = new NewsSourceEntity();
            source.setId(sourceJsonObject.getLong("sourceId"));
            source.setName(sourceJsonObject.getString("sourceName"));
            source.setIconUrl(sourceJsonObject.getString("iconUrl"));
            source.setAccentColorCode(sourceJsonObject.getString("sourceAccentColorCode"));

            news.setNewsSourceEntity(source);
            this.newsList.add(news);

        }

        return this.newsList;
    }


    public int getStartIndex(int pageNumber) {
        return pageNumber * this.pageSize - this.pageSize + 1;
    }
}
