package net.toracode.moviedb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import net.toracode.moviedb.adapters.RecyclerAdapter;
import net.toracode.moviedb.commons.Pref;
import net.toracode.moviedb.entity.Movie;
import net.toracode.moviedb.service.Commons;

public class OfflineActivity extends AppCompatActivity {

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

        String key = getIntent().getStringExtra("key");
        if (this.getMovieList(key) != null)
            this.setupRecyclerView(this.getMovieList(key),key);
        else
            Commons.showDialog(this, "It's lonely here", "Seems like your haven't saved any item yet!");

    }

    private List<Movie> getMovieList(String key) {
        String movieListJson = Pref.getPreferenceString(this, key);
        if (movieListJson != null && !movieListJson.equals("")) {
            Gson gson = new Gson();
            return gson.fromJson(movieListJson, new TypeToken<List<Movie>>() {
            }.getType());
        }
        return null;
    }

    private void setupRecyclerView(List<Movie> movieList,String key) {
        recyclerView.setNestedScrollingEnabled(false);
        RecyclerAdapter adapter = new RecyclerAdapter(this,movieList);
        adapter.setPrefKey(key);
        recyclerView.setAdapter(adapter);
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
