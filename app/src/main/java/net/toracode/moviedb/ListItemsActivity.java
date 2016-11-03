package net.toracode.moviedb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.github.johnpersano.supertoasts.library.SuperToast;
import com.google.gson.Gson;

import net.toracode.moviedb.adapters.RecyclerAdapter;
import net.toracode.moviedb.commons.ItemClickSupport;
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

public class ListItemsActivity extends AppCompatActivity {

    @BindView(R.id.listItemsRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private Long listId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        this.listId = getIntent().getLongExtra("listId", 0);
        this.getListByListId(listId);
    }

    private void getListByListId(Long listId) {
        final String url = getResources().getString(R.string.baseUrl) + "list/" + listId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(ListItemsActivity.this).fetchGetResponse(url);
                    final String responseBody = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_NOT_FOUND)
                                Commons.showSimpleToast(getApplicationContext(), "List not found!");
                            else if (response.code() == ResourceProvider.RESPONSE_CODE_OK) {
                                List<Movie> movieList = parseMovieList(responseBody);
                                loadListItems(movieList);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e("CUSTOM_LIST_BY_ID", e.toString());
                }
            }
        }).start();

    }

    private void loadListItems(final List<Movie> movieList) {
        if (movieList != null) {
            recyclerView.setAdapter(new RecyclerAdapter(this, movieList));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setVisibility(View.VISIBLE);
            if (progressBar.getVisibility() == View.VISIBLE)
                progressBar.setVisibility(View.GONE);
            ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    startActivity(new Intent(ListItemsActivity.this, DetailsActivity.class).putExtra("movie", movieList.get(position))
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClicked(final RecyclerView recyclerView, final int position, View v) {
                    new AlertDialog.Builder(ListItemsActivity.this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle("Remove item")
                            .setMessage("Are you sure you want to remove this movie from this list?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.v("POSITION", position + "");
                                    removeFromList(movieList.get(position));
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return false;
                }
            });
        }
    }

    private void removeFromList(final Movie movie) {
        final SuperToast loadingToast = Commons.getLoadingToast(this);
        loadingToast.show();
        final String url = getResources().getString(R.string.baseUrl) + "list/" + this.listId + "/remove/" + movie.getUniqueId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(ListItemsActivity.this).fetchPostResponse(url);
                    final String responseBody = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingToast.dismiss();
                            if (response.code() == ResourceProvider.RESPONSE_CODE_INTERNAL_SERVER_ERROR) {
                                Commons.showSimpleToast(getApplicationContext(), "Can not remove item.");
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_OK) {
                                Commons.showSimpleToast(getApplicationContext(), movie.getName() + " is removed from this list.");
                                List<Movie> updatedMovieList = parseMovieList(responseBody);
                                loadListItems(updatedMovieList);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e("REMOVE_FROM_LIST", e.toString());
                }
            }
        }).start();
    }

    private List<Movie> parseMovieList(String responseBody) {
        Gson gson = Commons.buildGson();
        List<Movie> movieList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            for (int i = 0; i < jsonArray.length(); i++) {
                Movie movie = gson.fromJson(jsonArray.getJSONObject(i).toString(), Movie.class);
                movieList.add(movie);
            }
        } catch (JSONException e) {
            Log.e("PARSE_LIST_MOVIES", e.toString());
        }

        return movieList;
    }

//    public void remove(List<Movie> movieList,int position) {
//        if (position < 0 || position >= movieList.size()) {
//            return;
//        }
//        movieList.remove(position);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
