package net.toracode.moviedb.fragmants;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.toracode.moviedb.R;
import net.toracode.moviedb.adapters.ReviewRecyclerAdapter;
import net.toracode.moviedb.entity.Review;
import net.toracode.moviedb.service.ResourceProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Response;

public class ReviewFragment extends Fragment {
    private static final String ARG_MOVIE_ID = "param1";

    private String movieId;

    private RecyclerView reviewRecyclerView;
    private int page = 0;
    private int size = 10;

    public ReviewFragment() {
        // Required empty public constructor
    }

    public static ReviewFragment newInstance(Long movieId) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MOVIE_ID, String.valueOf(movieId));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movieId = getArguments().getString(ARG_MOVIE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_review, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.reviewRecyclerView = (RecyclerView) getView().findViewById(R.id.reviewRecyclerView);

        this.fetchReviews();
    }

    private void fetchReviews() {
        final String url = this.buildUrl();
        Log.d("URL_REVIEW",url);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = new ResourceProvider(getActivity()).fetchGetResponse(url);
                    onResponse(response);
                } catch (IOException e) {
                    Log.d("Ex",e.toString());
                }
            }
        }).start();
    }

    private void onResponse(Response response) throws IOException {
        if (response != null && response.code() == ResourceProvider.RESPONSE_CODE_FOUND) {
            final List<Review> reviewList = this.parseJson(response.body().string());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    reviewRecyclerView.setAdapter(new ReviewRecyclerAdapter(getActivity(),reviewList));
                    reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    reviewRecyclerView.setNestedScrollingEnabled(false);
                }
            });

        }
    }

    private List<Review> parseJson(String responseString) {
        List<Review> reviewList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseString);
            for (int i = 0; i < jsonArray.length(); i++) {
                // Creates the json object which will manage the information received
                GsonBuilder builder = new GsonBuilder();

                // Register an adapter to manage the date types as long values
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                Review review = gson.fromJson(jsonArray.getJSONObject(i).toString(),Review.class);
                reviewList.add(review);
            }
        } catch (JSONException e) {
            Log.e("EX", e.toString());
        }
        return reviewList;
    }

    private String buildUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getResources().getString(R.string.baseUrl) + "review/movie/" + this.movieId);
        stringBuilder.append("?page=" + this.page + "&size=" + this.size);
        return stringBuilder.toString();
    }
}
