package net.toracode.moviedb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.facebook.accountkit.AccountKit;
import com.google.gson.Gson;

import net.toracode.moviedb.adapters.ReviewRecyclerAdapter;
import net.toracode.moviedb.entity.Review;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

public class MyReviewsActivity extends AppCompatActivity {

    @BindView(R.id.myReviewsRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private List<Review> reviewList;

    private int page = 0;
    private int size = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        if (AccountKit.getCurrentAccessToken() != null)
            this.loadReviews(AccountKit.getCurrentAccessToken().getAccountId());
        else
            this.startActivity(new Intent(this, PreferenceActivity.class));

    }

    private void loadReviews(final String accountId) {
        final String url = getResources().getString(R.string.baseUrl) + "review/user/" + accountId + "?page=" + this.page + "&size=" + this.size;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(MyReviewsActivity.this).fetchGetResponse(url);
                    final String responseBody = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_OK) {
                                reviewList = parseReviewList(responseBody);
                                setupRecyclerView(reviewList, accountId);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e("FETCH_USER_REVIEWS", e.toString());
                }
            }
        }).start();
    }

    private void setupRecyclerView(List<Review> reviewList, String accountId) {
        ReviewRecyclerAdapter adapter = new ReviewRecyclerAdapter(this, reviewList);
        adapter.setAccountId(accountId);
        this.recyclerView.setAdapter(adapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (this.progressBar.getVisibility() == View.VISIBLE)
            this.progressBar.setVisibility(View.GONE);
        this.recyclerView.setVisibility(View.VISIBLE);
    }

    private List<Review> parseReviewList(String responseBody) {
        List<Review> reviewList = new ArrayList<>();
        Gson gson = Commons.buildGson();
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            for (int i = 0; i < jsonArray.length(); i++)
                reviewList.add(gson.fromJson(jsonArray.getJSONObject(i).toString(), Review.class));
        } catch (JSONException e) {
            Log.e("PARSE_REVIEW_LIST", e.toString());
        }
        return reviewList;
    }

}
