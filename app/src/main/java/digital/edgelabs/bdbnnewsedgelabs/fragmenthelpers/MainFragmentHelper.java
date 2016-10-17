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

import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.adapters.RecyclerAdapter;
import digital.edgelabs.bdbnnewsedgelabs.commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.entity.Movie;
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
    private static int VP_PAGE_NUMBER = 0;
    private ProgressBar progressBar;

    private Button moreButton;

    private int pageIndex = 1;
    private int pageSize = 10;
    private List<Movie> movieList = new ArrayList<>();
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
        Log.d("SECTION_NUMBER", String.valueOf(pageNumber));
//        Toast.makeText(context, url, Toast.LENGTH_LONG).show();

        this.fetchNews(VP_PAGE_NUMBER, this.pageIndex);

    }


    private void fetchNews(final int vpPageNumber, final int pageIndex) {
        this.toast.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        final String url = buildUrl(context, vpPageNumber, pageIndex);
//                        final String url = context.getResources().getString(R.string.newsUrl);
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

    private String buildUrl(Activity context, int vpPageNumber, int pageIndex) {
        // Build request url with filter
        // get news source url from sharedpref and send them as param
        String[] categories = context.getResources().getStringArray(R.array.categories);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getResources().getString(R.string.baseUrl))
                .append(categories[vpPageNumber])
        .append("/page/"+pageIndex);
        Log.i("URL++",stringBuilder.toString());
        return stringBuilder.toString();
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
            this.movieList = this.parseJson(response);
//            if (this.newsList.size()>pageSize){
//                RecyclerAdapter adapter = new RecyclerAdapter(context,this.newsList);
//                adapter.notifyDataSetChanged();
//                recyclerView.setAdapter(adapter);
//            }else {
            this.setUpRecyclerView(context, movieList, vpPageNumber);
//            }

            this.progressBar.setVisibility(View.GONE);
            this.moreButton.setVisibility(View.VISIBLE);
            this.moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    startIndex += pageSize;
                    pageIndex++;
                    fetchNews(vpPageNumber, pageIndex);
                }
            });
//            textView.setText(categoryEntity.toString());
        } catch (JSONException e) {
            Log.d("JSON_EX", e.toString());
        } catch (ParseException e) {
            Log.e("PARSE_DATE_SDF", e.toString());
        }
    }


    private void setUpRecyclerView(final Activity context, List<Movie> newsList, final int vpPageNumber) {
        final RecyclerAdapter adapter = new RecyclerAdapter(context, this.movieList);
        if (this.movieList.size() > pageSize) {
            adapter.notifyDataSetChanged();
        }
        recyclerView.setAdapter(adapter);
        recyclerView.stopNestedScroll();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private List<Movie> parseJson(String response) throws JSONException, ParseException {

        // FILL UP MOVIE LIST
        Movie movie = new Movie();
        movie.setName("First Movie");
        movie.setDirectorName("First Director");
        movie.setImageUrl("http://dummyimage.com/150x150/ddd/fff.png");
        movie.setCast("Cast list");
        this.movieList.add(movie);
        movie= new Movie();
        movie.setName("Second Movie");
        movie.setDirectorName("Second Director");
        movie.setImageUrl("http://dummyimage.com/150x150/ddd/fff.png");
        movie.setCast("Cast list Second");
        this.movieList.add(movie);

        return this.movieList;
    }


    public int getStartIndex(int pageNumber) {
        return pageNumber * this.pageSize - this.pageSize + 1;
    }
}
