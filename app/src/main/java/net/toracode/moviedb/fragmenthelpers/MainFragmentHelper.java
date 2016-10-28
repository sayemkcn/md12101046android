package net.toracode.moviedb.fragmenthelpers;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.github.johnpersano.supertoasts.library.SuperToast;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import net.toracode.moviedb.R;
import net.toracode.moviedb.adapters.RecyclerAdapter;
import net.toracode.moviedb.entity.Movie;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

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
    }

    public void exec(int pageNumber) {
        this.VP_PAGE_NUMBER = pageNumber;
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
                        final String response = new ResourceProvider(context).fetchData(url);
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
                .append("/page/" + pageIndex);
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

                    this.movieList.add(movie);
                }
            }
        } catch (Exception e) {
            Log.e("CAN_NOT_PARSE", e.getMessage());
        }
        return this.movieList;
    }


    public int getStartIndex(int pageNumber) {
        return pageNumber * this.pageSize - this.pageSize + 1;
    }
}
