package digital.edgelabs.bdbnnewsedgelabs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsSourceEntity;
import digital.edgelabs.bdbnnewsedgelabs.events.NewsItemClickEvent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // register eventbus
        ButterKnife.bind(this);

        Long newsId = getIntent().getLongExtra("newsId",0);

        Log.d("NEWS_ID",newsId+"");

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
                        Log.d("HTTP_EX", e.toString());
                    }
                }
            }
        }).start();


    }

    private void onResponse(String response){
        try {
            this.updateViews(this.parseNewsEntity(response));
        } catch (JSONException e) {
            Log.d("JSON_EX",e.toString());
        } catch (ParseException e) {
            Log.d("PARSE_EX",e.toString());
        }
    }

    private void updateViews(NewsEntity news) {
        Glide.with(this).load(news.getImageUrl()).into(this.detailsImageView);
        Glide.with(this).load(news.getNewsSourceEntity().getIconUrl()).into(this.sourceIconImageView);
        this.titleTextView.setText(news.getTitle());
        this.sourceNameTextView.setText(news.getNewsSourceEntity().getName());
        this.authorTextView.setText(news.getAuthor());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(news.getLastUpdated());
        this.timeTextView.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + ", " + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + " " + calendar.get(Calendar.DAY_OF_MONTH));
        this.detailsTextView.setText(news.getDetails());
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

    private void setNews() {

    }


}
