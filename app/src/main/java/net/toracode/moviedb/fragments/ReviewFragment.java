package net.toracode.moviedb.fragments;


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
import android.widget.ProgressBar;
import android.widget.RatingBar;

import com.facebook.accountkit.AccountKit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.toracode.moviedb.DetailsActivity;
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
    private static final String ARG_MOVIE_ID = "movieId";
    private static final String ARG_ACCOUNT_ID = "accountId";

    private Long movieId;
    private String accountId;
    private View reviewBoxLayout;

    private EditText reviewTitleEditText;
    private EditText reviewMessageEditText;
    private RatingBar ratingBar;
    private Button postReviewButton;
    private Button registerButton;
    private Button loadMoreButton;
    private ProgressBar progressBar;

    private RecyclerView reviewRecyclerView;
    private int page = 0;
    private int size = 10;

    List<Review> reviewList = new ArrayList<>();

    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.movieId = getArguments().getLong(ARG_MOVIE_ID);
            this.accountId = getArguments().getString(ARG_ACCOUNT_ID);
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
        this.loadMoreButton = (Button) getView().findViewById(R.id.loadMoreButton);
        this.progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);

        // Show review box if user logged in.
        if (AccountKit.getCurrentAccessToken() != null) {
            this.registerButton.setVisibility(View.GONE);
            this.reviewBoxLayout.setVisibility(View.VISIBLE);
        }

        this.fetchReviews(this.buildUrl());
        // post review
        this.postReviewButton.setOnClickListener(this);
        this.registerButton.setOnClickListener(this);
        this.loadMoreButton.setOnClickListener(this);

    }

    private void fetchReviews(final String url) {
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
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    reviewRecyclerView.setVisibility(View.VISIBLE);
                }
            });

        if (response != null && (response.code() == ResourceProvider.RESPONSE_CODE_FOUND || response.code() == ResourceProvider.RESPONSE_CODE_OK)) {
            final ResponseBody responseBody = response.body();
            final String responseBodyString = responseBody.string();
            responseBody.close(); // Look! I didn't forget to close the connections!
            final List<Review> reviewList = this.parseJson(responseBodyString);
            response.close();
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // if response empty.
                        if (responseBodyString.length() < 10) {
                            if (getActivity() != null && loadMoreButton.getVisibility() == View.VISIBLE) {
                                Commons.showDialog(getActivity(), "No reviews!", "Looks like you have  no other reviews left!");
                                loadMoreButton.setVisibility(View.GONE);
                            }
                        } else if (!(getActivity() instanceof DetailsActivity))
                            loadMoreButton.setVisibility(View.VISIBLE);
                        ReviewRecyclerAdapter adapter = new ReviewRecyclerAdapter(getActivity(), reviewList);
                        if (AccountKit.getCurrentAccessToken() != null)
                            adapter.setAccountId(AccountKit.getCurrentAccessToken().getAccountId());
                        reviewRecyclerView.setAdapter(adapter);
                        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        reviewRecyclerView.setNestedScrollingEnabled(false);
                        reviewRecyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    private List<Review> parseJson(String responseString) {
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
                this.reviewList.add(review);
            }
        } catch (JSONException e) {
            Log.e("EX", e.toString());
        }
        return reviewList;
    }

    // build url according to the fragment argumanets.
    private String buildUrl() {
        Log.i("MOV ACC", this.movieId + "  " + this.accountId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getResources().getString(R.string.baseUrl));
        if (this.movieId != null && this.accountId == null) {
            // url for reviews for a movie
            stringBuilder.append("review/movie/" + this.movieId);
            // hide load more button from review fragment on movie details
            this.loadMoreButton.setVisibility(View.GONE);
            this.size = 20;
        } else if (this.accountId != null && (this.movieId == null || this.movieId == 0)) {
            // my reviews url
            stringBuilder.append("review/user/" + accountId);
            this.reviewBoxLayout.setVisibility(View.GONE);
        }
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
        } else if (id == R.id.loadMoreButton) {
            this.page++;
            this.fetchReviews(this.buildUrl());
        }
    }

    private void postReview(String accountId) {
        String title = this.reviewTitleEditText.getText().toString();
        String message = this.reviewMessageEditText.getText().toString();
        float rating = this.ratingBar.getRating();
        if (this.isValid(title, message, rating)) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getString(R.string.loadingText));
            progressDialog.show();
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
                                if (progressDialog.isShowing()) progressDialog.cancel();
                            }
                        });
                    } catch (IOException e) {
                        if (progressDialog.isShowing()) progressDialog.cancel();
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
                    fetchReviews(buildUrl());
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
