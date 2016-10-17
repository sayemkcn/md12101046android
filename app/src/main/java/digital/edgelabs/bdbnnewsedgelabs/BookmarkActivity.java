package digital.edgelabs.bdbnnewsedgelabs;

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
import digital.edgelabs.bdbnnewsedgelabs.commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.adapters.RecyclerAdapter;
import digital.edgelabs.bdbnnewsedgelabs.entity.Movie;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;

public class BookmarkActivity extends AppCompatActivity {
    @BindView(R.id.bookmarkRecyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        if (this.getMovieList() != null)
            this.setupRecyclerView(this.getMovieList());
        else
            Commons.showDialog(this, "It's lonely here", "Seems like your bookmark list is empty. First add some articles and then proceed!");

    }

    private List<Movie> getMovieList() {
        String movieListJson = Pref.getPreferenceString(this, Pref.PREF_KEY_BOOKMARK_LIST);
        if (movieListJson != null && !movieListJson.equals("")) {
            Gson gson = new Gson();
            return gson.fromJson(movieListJson, new TypeToken<List<Movie>>() {
            }.getType());
        }
        return null;
    }

    private void setupRecyclerView(List<Movie> movieList) {
        recyclerView.setAdapter(new RecyclerAdapter(this, movieList));
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
