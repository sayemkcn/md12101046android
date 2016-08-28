package digital.edgelabs.bdbnnewsedgelabs;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.adapters.RecyclerAdapter;
import digital.edgelabs.bdbnnewsedgelabs.commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;

public class OfflineNewsActivity extends AppCompatActivity {

    @BindView(R.id.offlineNewsRecyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        if (this.getNewsList() != null)
            this.setupRecyclerView(this.getNewsList());
        else
            Commons.showDialog(this, "It's lonely here", "Seems like your haven't saved any item yet!");

    }

    private List<NewsEntity> getNewsList() {
        String newsListJson = Pref.getPreferenceString(this, Pref.PREF_KEY_OFFLINE_NEWS_LIST);
        if (newsListJson != null && !newsListJson.equals("")) {
            Gson gson = new Gson();
            return gson.fromJson(newsListJson, new TypeToken<List<NewsEntity>>() {
            }.getType());
        }
        return null;
    }

    private void setupRecyclerView(List<NewsEntity> newsList) {
        recyclerView.setAdapter(new RecyclerAdapter(this, newsList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            this.finish();
        return super.onOptionsItemSelected(item);
    }
}
