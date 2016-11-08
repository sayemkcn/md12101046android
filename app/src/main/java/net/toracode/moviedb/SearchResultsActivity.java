package net.toracode.moviedb;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.toracode.moviedb.adapters.RecyclerAdapter;
import net.toracode.moviedb.entity.Movie;
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
import okhttp3.ResponseBody;

public class SearchResultsActivity extends AppCompatActivity {

    @BindView(R.id.searchResultsRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.noItemsFoundTextView)
    TextView noItemsFoundTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_activity, menu);
        // expand item
        MenuItem item = menu.findItem(R.id.action_search);
        item.expandActionView();
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty())
                    fetchMatchedData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    String lastChar = newText.substring(newText.length() - 1);
                    if (lastChar.equals(" "))
                        fetchMatchedData(newText);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            this.finish();
        return super.onOptionsItemSelected(item);
    }

    private void fetchMatchedData(String phrase) {
        this.progressBar.setVisibility(View.VISIBLE);
        this.recyclerView.setVisibility(View.GONE);
        this.noItemsFoundTextView.setVisibility(View.GONE);

        final String url = getResources().getString(R.string.baseUrl) + "movie/search/" + phrase;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(SearchResultsActivity.this).fetchGetResponse(url);
                    ResponseBody responseBody = response.body();
                    final String responseBodyString = responseBody.string();
                    responseBody.close(); //close connection
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_NO_CONTENT) {
                                progressBar.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                noItemsFoundTextView.setVisibility(View.VISIBLE);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                noItemsFoundTextView.setVisibility(View.GONE);
                                List<Movie> movieList = parseMovieList(responseBodyString);
                                setupRecyclerView(movieList);
                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setupRecyclerView(List<Movie> movieList) {
        if (movieList != null) {
            recyclerView.setAdapter(new RecyclerAdapter(this, movieList));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setVisibility(View.VISIBLE);
            if (progressBar.getVisibility() == View.VISIBLE)
                progressBar.setVisibility(View.GONE);
//            ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//                @Override
//                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
//                    startActivity(new Intent(ListItemsActivity.this, DetailsActivity.class).putExtra("movie", movieList.get(position))
//                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                }
//            }).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
//                @Override
//                public boolean onItemLongClicked(final RecyclerView recyclerView, final int position, View v) {
//                    new AlertDialog.Builder(ListItemsActivity.this)
//                            .setIcon(R.mipmap.ic_launcher)
//                            .setTitle("Remove item")
//                            .setMessage("Are you sure you want to remove this movie from this list?")
//                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    Log.v("POSITION", position + "");
//                                    removeFromList(movieList.get(position));
//                                }
//                            })
//                            .setNegativeButton("No", null)
//                            .show();
//                    return false;
//                }
//            });
        }
    }

    private List<Movie> parseMovieList(String responseBody) {
        Gson gson = Commons.buildGson();
        List<Movie> movieList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            for (int i = 0; i < jsonArray.length(); i++) {
                Movie movie = gson.fromJson(jsonArray.getJSONObject(i).toString(), Movie.class);
                movie.setImageUrl(getResources().getString(R.string.baseUrl) + "movie/image/" + movie.getUniqueId());
                movieList.add(movie);
            }
        } catch (JSONException e) {
            Log.e("PARSE_LIST_MOVIES", e.toString());
        }

        return movieList;
    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
