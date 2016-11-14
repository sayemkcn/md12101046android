package net.toracode.moviedb.fragmenthelpers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.facebook.accountkit.AccountKit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import net.toracode.moviedb.DetailsActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.adapters.CustomListAdapter;
import net.toracode.moviedb.adapters.FeaturedRecyclerAdapter;
import net.toracode.moviedb.adapters.RecyclerAdapter;
import net.toracode.moviedb.commons.CustomSliderView;
import net.toracode.moviedb.commons.Pref;
import net.toracode.moviedb.commons.SliderChildAnimator;
import net.toracode.moviedb.entity.CustomList;
import net.toracode.moviedb.entity.Movie;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class FeaturedFragmentHelper {
    private Activity context;
    private static int VP_PAGE_NUMBER = 0;
    private RecyclerView featuredOfflineRecyclerView;
    private RecyclerView featuredMyListRecyclerView;
    private RecyclerView featuredPublicListRecyclerView;
    private TextView noItemsTextOffline;
    private SliderLayout sliderLayout;

    private View featuredNewsLayout;
//    private ProgressBar progressBar;


    public FeaturedFragmentHelper(Activity context, View rootView) {
        this.context = context;
        ButterKnife.bind(context);
        this.featuredOfflineRecyclerView = (RecyclerView) rootView.findViewById(R.id.featuredOfflineRecyclerView);
        this.featuredMyListRecyclerView = (RecyclerView) rootView.findViewById(R.id.featuredPublicListsRecyclerView);
        this.featuredPublicListRecyclerView = (RecyclerView) rootView.findViewById(R.id.featuredFeaturedRecyclerView);
        this.noItemsTextOffline = (TextView) rootView.findViewById(R.id.offline_no_items_text);
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
        TextView savedTitleTextView = (TextView) rootView.findViewById(R.id.savedTitleText);
        TextView offlineTitleTextView = (TextView) rootView.findViewById(R.id.myListText);
        featuredTitleTextView.setTypeface(typeface);
        savedTitleTextView.setTypeface(typeface);
        offlineTitleTextView.setTypeface(typeface);
    }

    public void exec(int pageNumber) {
        this.VP_PAGE_NUMBER = pageNumber;
        Log.d("SECTION_NUMBER", String.valueOf(pageNumber));

        this.fetchOfflineItems();
        this.fetchSliderItems();
        this.fetchFeaturedItems();
        this.fetchPublicLists();
    }

    private void fetchPublicLists() {
            final String url = context.getResources().getString(R.string.baseUrl) + "list/public?page=0";
            Log.d("URL_PUB",url);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Response response = new ResourceProvider(context).fetchGetResponse(url);
                        final ResponseBody responseBody = response.body();
                        final String responseBodyString = responseBody.string();
                        responseBody.close(); // fucking closed the fucking connection.
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.code() == ResourceProvider.RESPONSE_CODE_OK) {
                                    List<CustomList> listOfCustomList = parseCustomList(responseBodyString);
                                    setUpCustomListRecyclerView(featuredMyListRecyclerView, listOfCustomList);
                                }
                            }
                        });

                    } catch (IOException e) {
                        Log.e("GET_LISTS", e.toString());
                    }
                }
            }).start();
    }

    private List<CustomList> parseCustomList(String jsonArrayString) {
        // Creates the json object which will manage the information received
        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        Gson gson = builder.create();
        List<CustomList> listOfCustomList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for (int i = 0; i < jsonArray.length(); i++) {
                CustomList list = gson.fromJson(jsonArray.getJSONObject(i).toString(), CustomList.class);
                listOfCustomList.add(list);
            }
        } catch (JSONException e) {
            Log.e("LIST_JSON_PERSON", e.toString());
        }
        return listOfCustomList;
    }

    private void fetchFeaturedItems() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = context.getResources().getString(R.string.featuredContentUrl);
                try {
                    final String response = new ResourceProvider(context).fetchData(url);
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

    private void fetchSliderItems() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = context.getResources().getString(R.string.sliderContentUrl);
                try {
                    final Response response = new ResourceProvider(context).fetchGetResponse(url);
                    final ResponseBody responseBody = response.body();
                    final String responseBodyString = responseBody.string();
                    responseBody.close(); // closed the fucking connection.
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_NO_CONTENT)
                                sliderLayout.setVisibility(View.GONE);
                            else if (response.code() == ResourceProvider.RESPONSE_CODE_OK)
                                onResponse(url, responseBodyString);
                        }
                    });
                } catch (IOException e) {
                    Log.e("IOExSlider", e.toString());
                }
            }
        }).start();

    }

    private void onResponse(String url, String response) {
        try {
            List<Movie> movieList = this.parseJson(response);
            if (url.equals(context.getResources().getString(R.string.sliderContentUrl)))
                this.updateSlider(movieList);
            else if (url.equals(context.getResources().getString(R.string.featuredContentUrl)))
                this.updateFeaturedItems(movieList);

        } catch (JSONException e) {
            Log.e("JSONEx", e.getMessage());
        } catch (ParseException e) {
            Log.e("ParseEx", e.getMessage());

        }
    }

    private void updateFeaturedItems(List<Movie> movieList) {
        // WORK HERE
        this.featuredPublicListRecyclerView.setAdapter(new FeaturedRecyclerAdapter(context, movieList));
        this.featuredPublicListRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        this.featuredNewsLayout.setVisibility(View.VISIBLE);
        this.featuredPublicListRecyclerView.setNestedScrollingEnabled(false);
    }


    private List<Movie> parseJson(String response) throws JSONException, ParseException {
        List<Movie> movieList = new ArrayList<>();
        try {
            Type listType = new TypeToken<List<Movie>>() {
            }.getType();
            movieList = Commons.buildGson().fromJson(response, listType);
        } catch (Exception e) {
//            Log.e("CAN_NOT_PARSE", e.getMessage());
        }
        return movieList;
    }

    private void fetchOfflineItems() {
        List<Movie> offlineItemList = this.getOfflineItemList();
        // load offline news
        if (offlineItemList == null || offlineItemList.isEmpty()) {
            this.noItemsTextOffline.setVisibility(View.VISIBLE);
        } else {
            // if list not empty
            Collections.reverse(offlineItemList);
            // Limit 3 news items
            if (offlineItemList.size() > 3) {
                this.setUpRecyclerView(this.featuredOfflineRecyclerView, offlineItemList.subList(0, 3));
            } else {
                this.setUpRecyclerView(this.featuredOfflineRecyclerView, offlineItemList);
            }
        }

    }

    private void setUpRecyclerView(RecyclerView recyclerView, List<Movie> movieList) {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(new RecyclerAdapter(this.context, movieList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.context));
        recyclerView.setNestedScrollingEnabled(false);
//        this.recyclerView.setVisibility(View.VISIBLE);
    }

    private void setUpCustomListRecyclerView(RecyclerView recyclerView, List<CustomList> listOfCustomList) {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(new CustomListAdapter(this.context, listOfCustomList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.context));
        recyclerView.setNestedScrollingEnabled(false);
//        this.recyclerView.setVisibility(View.VISIBLE);
    }

    private List<Movie> getOfflineItemList() {
        String newsListJson = Pref.getPreferenceString(context, Pref.PREF_KEY_OFFLINE_LIST);
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
                CustomSliderView textSliderView = new CustomSliderView(context, "");
                // initialize a SliderLayout
                textSliderView
                        .description(item.getName())
                        .image(context.getResources().getString(R.string.baseUrl) + "movie/image/" + item.getUniqueId())
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
