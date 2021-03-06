package net.toracode.moviebuzz.fragmenthelpers;

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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.toracode.moviebuzz.R;
import net.toracode.moviebuzz.adapters.RecyclerAdapter;
import net.toracode.moviebuzz.entity.Movie;
import net.toracode.moviebuzz.service.Commons;
import net.toracode.moviebuzz.service.ResourceProvider;

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
    //    private static int VP_PAGE_NUMBER = 0;
    private ProgressBar progressBar;

    private Button moreButton;

    private int pageIndex = 0;
    private int pageSize = 10;
    private List<Movie> movieList = new ArrayList<>();
    private SuperToast toast;

    public MainFragmentHelper(Activity context, View rootView) {
        this.context = context;
        ButterKnife.bind(context);
        this.toast = Commons.getLoadingToast(context);
        this.recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        this.progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        this.moreButton = (Button) rootView.findViewById(R.id.moreButton);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
        this.moreButton.setTypeface(typeface);
    }

    public void exec(int pageNumber) {
        this.fetchNews(pageNumber, this.pageIndex);
    }


    private void fetchNews(final int vpPageNumber, final int pageIndex) {
        this.toast.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        final String url = buildUrl(context, vpPageNumber, pageIndex);
                        Log.i("URL_LOL", url);
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
                                Commons.showNetworkUnavailableDialog(context, "Connection Problem!", context.getResources().getString(R.string.internetUnavailableMessage));
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private String buildUrl(Activity context, int vpPageNumber, int pageIndex) {
        String[] categories = context.getResources().getStringArray(R.array.categories_url_attr);
        if (vpPageNumber <= 2)
            return context.getResources().getString(R.string.baseUrl) +
                    "movie/type/" +
                    categories[vpPageNumber] +
                    "?page=" + pageIndex + "&size=" + pageSize;
        return context.getResources().getString(R.string.baseUrl) + "movie/industry/" +
                categories[vpPageNumber] +
                "?page=" + pageIndex + "&size=" + pageSize;
    }

    private void onResponse(String response, final int vpPageNumber) {
        try {
            this.movieList = this.parseJson(response);
            this.setUpRecyclerView(context, movieList, vpPageNumber);
            this.progressBar.setVisibility(View.GONE);
            this.moreButton.setVisibility(View.VISIBLE);
            this.moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pageIndex++;
                    fetchNews(vpPageNumber, pageIndex);
                }
            });
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
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Movie movie = gson.fromJson(jsonObject.toString(), Movie.class);
                movie.setImageUrl(context.getResources().getString(R.string.baseUrl) + "movie/image/" + movie.getUniqueId());
                this.movieList.add(movie);
            }

        } catch (Exception e) {
            Log.e("CAN_NOT_PARSE", e.toString());
        }
        return this.movieList;
    }


}
