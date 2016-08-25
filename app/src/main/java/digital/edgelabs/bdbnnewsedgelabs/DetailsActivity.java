package digital.edgelabs.bdbnnewsedgelabs;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.Commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsSourceEntity;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;
import digital.edgelabs.bdbnnewsedgelabs.service.NewsProvider;

public class DetailsActivity extends AppCompatActivity {
    @BindView(R.id.newsDetailsImageView)
    ImageView detailsImageView;
    @BindView(R.id.newsSourceIconTextView)
    ImageView sourceIconImageView;
    @BindView(R.id.newsTitleTextView)
    TextView titleTextView;
    @BindView(R.id.newsSourceNameTextView)
    TextView sourceNameTextView;
    @BindView(R.id.newsAuthorTextView)
    TextView authorTextView;
    @BindView(R.id.newsTimeTextView)
    TextView timeTextView;
    @BindView(R.id.newsDetailsTextView)
    TextView detailsTextView;

    @BindView(R.id.contentLayout)
    View contentLayout;
    @BindView(R.id.detailsProgressBar)
    ProgressBar progressBar;

    private NewsEntity news;

    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().hide();

        // register eventbus
        ButterKnife.bind(this);

        this.typeface = typeface.createFromAsset(getAssets(), "fonts/SolaimanLipi.ttf");

        Long newsId = getIntent().getLongExtra("newsId", 0);

        Log.d("NEWS_ID", newsId + "");

        final String url = getResources().getString(R.string.newsDetailsUrl);
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        final String response = new NewsProvider(DetailsActivity.this).fetchNews(url);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onResponse(response);
                            }
                        });
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Commons.showDialog(DetailsActivity.this, "Connection unavailable!", "Looks like your internet connection is too slow or there\'s no internet at all! Please connect to the internet first!");
                            }
                        });
                    }
                }
            }
        }).start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        } else if (id == R.id.action_bookmark) {
            if (this.news == null) {
                Commons.showDialog(this, "Wait a moment!", "Please wait while this news is fully loaded!");
            } else {
                Gson gson = new Gson();
                String newsListJson = Pref.getPreferenceString(this, Pref.PREF_KEY_BOOKMARK_LIST);
                List<NewsEntity> newsList;
                if (newsListJson == null || newsListJson.equals("")) {
                    newsList = new ArrayList<>();
                    Log.d("EXECUTED", "FUCK! " + newsListJson);
                } else {
                    newsList = gson.fromJson(newsListJson, new TypeToken<List<NewsEntity>>() {
                    }.getType());
//                    Log.d("EXECUTED", newsListJson);
                }

                this.news.setDetails(this.news.getDetails().substring(0, 100));
                newsList.add(this.news);
                newsListJson = gson.toJson(newsList);
                Pref.savePreference(this, Pref.PREF_KEY_BOOKMARK_LIST, newsListJson);
                this.showToast("Bookmark Added!");
//                Log.d("CONVERTED_STRING", newsList.size() + "  " + newsListJson);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.getView().setBackgroundColor(getResources().getColor(R.color.colorAccent));
        toast.getView().setPadding(20, 20, 20, 20);
        toast.show();
    }

    private void onResponse(String response) {
        try {
            if (this.progressBar.isShown()) {
                this.contentLayout.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.GONE);
            }
            this.news = this.parseNewsEntity(response);
            this.updateViews(this.news);
        } catch (JSONException e) {
            Commons.showDialog(this, "Connection unavailable!", "Looks like your internet connection is too slow or there\'s no internet connection at all! Please connect to the internet first!");
        } catch (ParseException e) {
            Log.d("PARSE_EX", e.toString());
        }
    }

    private void updateViews(NewsEntity news) {
        Glide.with(this).load(news.getImageUrl()).into(this.detailsImageView);
        Glide.with(this).load(news.getNewsSourceEntity().getIconUrl()).into(this.sourceIconImageView);
        this.titleTextView.setText(news.getTitle());
        this.sourceNameTextView.setText(news.getNewsSourceEntity().getName());
        this.authorTextView.setText(news.getAuthor() + " * ");
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(news.getLastUpdated());
        this.timeTextView.setText(Commons.computeTimeDiff(news.getLastUpdated(), new Date()).get(TimeUnit.HOURS).toString() + " " + getResources().getString(R.string.hourBefore));
        this.detailsTextView.setText(news.getDetails());

        // set typeface
        this.titleTextView.setTypeface(typeface);
        this.sourceNameTextView.setTypeface(typeface);
        this.authorTextView.setTypeface(typeface);
        this.timeTextView.setTypeface(typeface);
        this.detailsTextView.setTypeface(typeface);
    }

    private NewsEntity parseNewsEntity(String response) throws JSONException, ParseException {
        JSONObject jsonObject = new JSONObject(response);
        NewsEntity news = new NewsEntity();
        news.setId(jsonObject.getLong("id"));
        news.setTitle(jsonObject.getString("title"));
        news.setDetails(jsonObject.getString("details"));
        news.setImageUrl(jsonObject.getString("imageUrl"));
        news.setAuthor(jsonObject.getString("author"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        news.setLastUpdated(sdf.parse(jsonObject.getString("timestamp")));
//            Log.i("DATE",news.getLastUpdated().toString());

        JSONObject sourceJsonObject = jsonObject.getJSONObject("source");
        NewsSourceEntity source = new NewsSourceEntity();
        source.setId(sourceJsonObject.getLong("sourceId"));
        source.setName(sourceJsonObject.getString("sourceName"));
        source.setIconUrl(sourceJsonObject.getString("iconUrl"));
        source.setAccentColorCode(sourceJsonObject.getString("sourceAccentColorCode"));

        news.setNewsSourceEntity(source);
        return news;
    }


}
