package net.toracode.moviedb.fragmants;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.facebook.accountkit.AccountKit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.toracode.moviedb.PreferenceActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.adapters.ReviewRecyclerAdapter;
import net.toracode.moviedb.entity.Review;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class ReviewFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_MOVIE_ID = "param1";

    private String movieId;
    private View reviewBoxLayout;

    private EditText reviewTitleEditText;
    private EditText reviewMessageEditText;
    private RatingBar ratingBar;
    private Button postReviewButton;
    private Button registerButton;

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
        this.reviewBoxLayout = getView().findViewById(R.id.reviewBoxLayout);
        this.reviewTitleEditText = (EditText) getView().findViewById(R.id.reviewTitle);
        this.reviewMessageEditText = (EditText) getView().findViewById(R.id.reviewMessage);
        this.ratingBar = (RatingBar) getView().findViewById(R.id.ratingBar);
        this.postReviewButton = (Button) getView().findViewById(R.id.postReviewButton);
        this.registerButton = (Button) getView().findViewById(R.id.registerButton);

        // Show review box if user logged in.
        if (AccountKit.getCurrentAccessToken()!=null){
            this.registerButton.setVisibility(View.GONE);
            this.reviewBoxLayout.setVisibility(View.VISIBLE);
        }

        this.fetchReviews();
        // post review
        this.postReviewButton.setOnClickListener(this);
        this.registerButton.setOnClickListener(this);

    }

    private void fetchReviews() {
        final String url = this.buildUrl();
//        Log.d("URL_REVIEW", url);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = new ResourceProvider(getActivity()).fetchGetResponse(url);
                    onResponse(response);
                } catch (IOException e) {
                    Log.d("Ex", e.toString());
                }
            }
        }).start();
    }

    private void onResponse(Response response) throws IOException {
        if (response != null && response.code() == ResourceProvider.RESPONSE_CODE_FOUND) {
            final ResponseBody responseBody = response.body();
            final List<Review> reviewList = this.parseJson(responseBody.string());
            responseBody.close(); // Look! I didn't forget to close the connections!
            response.close();
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ReviewRecyclerAdapter adapter = new ReviewRecyclerAdapter(getActivity(), reviewList);
                        if (AccountKit.getCurrentAccessToken() != null)
                            adapter.setAccountId(AccountKit.getCurrentAccessToken().getAccountId());
                        reviewRecyclerView.setAdapter(adapter);
                        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        reviewRecyclerView.setNestedScrollingEnabled(false);
                    }
                });
            }
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
                Review review = gson.fromJson(jsonArray.getJSONObject(i).toString(), Review.class);
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.postReviewButton) {
            if (AccountKit.getCurrentAccessToken() != null)
                postReview(AccountKit.getCurrentAccessToken().getAccountId());
            else
                this.startActivity(new Intent(getActivity(), PreferenceActivity.class));
        } else if (id == R.id.registerButton) {
            if (AccountKit.getCurrentAccessToken() != null) {
                this.registerButton.setVisibility(View.GONE);
                this.reviewBoxLayout.setVisibility(View.VISIBLE);
                return;
            }
            this.startActivity(new Intent(getActivity(), PreferenceActivity.class));
        }
    }

    private void postReview(String accountId) {
        String title = this.reviewTitleEditText.getText().toString();
        String message = this.reviewMessageEditText.getText().toString();
        float rating = this.ratingBar.getRating();
        if (this.isValid(title, message, rating)) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getString(R.string.loadingText));
            final String url = getResources().getString(R.string.baseUrl) + "review/create?title=" + title + "&message=" + message + "&rating=" + rating + "&accountId=" + accountId + "&movieId=" + this.movieId;
//            Log.i("POST_REVIEW", url);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response response = new ResourceProvider(getActivity()).fetchPostResponse(url);
                        onPostReviewResponse(response);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog.isShowing())
                                    progressDialog.cancel();
                            }
                        });
                    } catch (IOException e) {
                        Log.e("POST_REVIEW", e.toString());
                    }
                }
            }).start();

        }
    }

    private void onPostReviewResponse(final Response response) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ratingBar.setRating(0f);
                if (response.code() == ResourceProvider.RESPONSE_CODE_LOCKED) {
                    Commons.showDialog(getActivity(), getResources().getString(R.string.alreadyPostedReviewTitle), getResources().getString(R.string.alreadyPostedReviewMessage));
                } else if (response.code() == ResourceProvider.RESPONSE_NOT_ACCEPTABLE) {
                    Commons.showSimpleToast(getActivity().getApplicationContext(), getResources().getString(R.string.movieOrUserNotFoundText));
                } else if (response.code() == ResourceProvider.RESPONSE_CODE_CREATED) {
                    reviewTitleEditText.setText("");
                    reviewMessageEditText.setText("");
                    fetchReviews();
                    Commons.showDialog(getActivity(), getResources().getString(R.string.reviewSuccessTitle), getResources().getString(R.string.reviewSuccessMessage));
                }
            }
        });

    }

    private boolean isValid(String title, String message, float rating) {
        if (title.isEmpty()) {
            this.reviewTitleEditText.setError("Review title can not be empty!");
            return false;
        }
        if (message.isEmpty()) {
            this.reviewMessageEditText.setError("Review message can not be empty!");
            return false;
        }
        if (rating == 0f) {
            Commons.showSimpleToast(getActivity().getApplicationContext(), "You have to choose a rating!");
            return false;
        }

        return true;
    }
}
