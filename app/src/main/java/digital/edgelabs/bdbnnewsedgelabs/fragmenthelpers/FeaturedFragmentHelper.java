package digital.edgelabs.bdbnnewsedgelabs.fragmenthelpers;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.adapters.RecyclerAdapter;
import digital.edgelabs.bdbnnewsedgelabs.commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class FeaturedFragmentHelper {
    private Activity context;
    private static int VP_PAGE_NUMBER = 0;
    private RecyclerView featuredOfflineRecyclerView;
    private RecyclerView featuredBookmarksRecyclerView;
    private TextView noItemsTextOffline;
    private TextView noItemsTextBookmarks;
//    private ProgressBar progressBar;


    public FeaturedFragmentHelper(Activity context, View rootView) {
        this.context = context;
        ButterKnife.bind(context);
        this.featuredOfflineRecyclerView = (RecyclerView) rootView.findViewById(R.id.featuredOfflineRecyclerView);
        this.featuredBookmarksRecyclerView = (RecyclerView) rootView.findViewById(R.id.featuredBookmarkedRecyclerView);
        this.noItemsTextOffline = (TextView) rootView.findViewById(R.id.offline_no_items_text);
        this.noItemsTextBookmarks = (TextView) rootView.findViewById(R.id.bookmarks_no_items_text);
    }

    public void exec(int pageNumber) {
        this.VP_PAGE_NUMBER = pageNumber;
        Log.d("SECTION_NUMBER", String.valueOf(pageNumber));

        this.fetchOfflineAndBookmarkedNews();

    }

    private void fetchOfflineAndBookmarkedNews() {
        List<NewsEntity> offlineNewsList = this.getOfflineNewsList();
        List<NewsEntity> bookmarkedNewsList = this.getBookmarkedNewsList();
        // load offline news
        if (offlineNewsList == null || offlineNewsList.isEmpty()) {
            this.noItemsTextOffline.setVisibility(View.VISIBLE);
        } else {
            // if list not empty
            Collections.reverse(offlineNewsList);
            // Limit 3 news items
            if (offlineNewsList.size() > 3) {
                this.setUpRecyclerView(this.featuredOfflineRecyclerView, offlineNewsList.subList(0, 3));
            } else {
                this.setUpRecyclerView(this.featuredOfflineRecyclerView, offlineNewsList);
            }
        }
        // load Bookmarks
        if (bookmarkedNewsList == null || bookmarkedNewsList.isEmpty()) {
            this.noItemsTextBookmarks.setVisibility(View.VISIBLE);
        } else {
            Collections.reverse(bookmarkedNewsList);
            // Limit 3 news Items
            if (bookmarkedNewsList.size() > 3) {
                this.setUpRecyclerView(this.featuredBookmarksRecyclerView, bookmarkedNewsList.subList(0, 3));
            } else {
                this.setUpRecyclerView(this.featuredBookmarksRecyclerView, bookmarkedNewsList);
            }
        }

    }

    private void setUpRecyclerView(RecyclerView recyclerView, List<NewsEntity> newsList) {
        recyclerView.setAdapter(new RecyclerAdapter(this.context, newsList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.context));
//        this.recyclerView.setVisibility(View.VISIBLE);
    }

    private List<NewsEntity> getOfflineNewsList() {
        String newsListJson = Pref.getPreferenceString(context, Pref.PREF_KEY_OFFLINE_NEWS_LIST);
        if (newsListJson != null && !newsListJson.equals("")) {
            Gson gson = new Gson();
            return gson.fromJson(newsListJson, new TypeToken<List<NewsEntity>>() {
            }.getType());
        }
        return null;
    }


    private List<NewsEntity> getBookmarkedNewsList() {
        String newsListJson = Pref.getPreferenceString(context, Pref.PREF_KEY_BOOKMARK_LIST);
        if (newsListJson != null && !newsListJson.equals("")) {
            Gson gson = new Gson();
            return gson.fromJson(newsListJson, new TypeToken<List<NewsEntity>>() {
            }.getType());
        }
        return null;
    }
}
