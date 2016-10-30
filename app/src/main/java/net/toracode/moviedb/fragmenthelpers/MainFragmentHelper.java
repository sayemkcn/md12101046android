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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import net.toracode.moviedb.R;
import net.toracode.moviedb.adapters.RecyclerAdapter;
import net.toracode.moviedb.entity.Movie;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class MainFragmentHelper {
    private Activity context;
    private RecyclerView recyclerView;
    private static int VP_PAGE_NUMBER = 0;
    private ProgressBar progressBar;

    private Button moreButton;

    private int pageIndex = 0;
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
                .append("movie/type/")
                .append(categories[vpPageNumber])
                .append("?page=" + pageIndex + "&size=" + pageSize);
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
//        Log.i("RESPONSE",response);
        try {
            // Creates the json object which will manage the information received
            GsonBuilder builder = new GsonBuilder();

            // Register an adapter to manage the date types as long values
            builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return new Date(json.getAsJsonPrimitive().getAsLong());
                }
            });

            Gson gson = builder.create();
            JSONArray jsonArray = new JSONArray(response);
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Movie movie = gson.fromJson(jsonObject.toString(),Movie.class);
                movie.setImageUrl(context.getResources().getString(R.string.baseUrl)+"movie/image/"+movie.getUniqueId());
                this.movieList.add(movie);
            }

//            Type listType = new TypeToken<List<Movie>>() {
//            }.getType();
//            this.movieList = gson.fromJson(response, listType);
        } catch (Exception e) {
            Log.e("CAN_NOT_PARSE", e.toString());
        }
        return this.movieList;
    }


    public int getStartIndex(int pageNumber) {
        return pageNumber * this.pageSize - this.pageSize + 1;
    }
}
