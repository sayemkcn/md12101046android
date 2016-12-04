package net.toracode.moviebuzz;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import net.toracode.moviebuzz.commons.CustomListOperations;
import net.toracode.moviebuzz.fragments.CommentsFragment;
import net.toracode.moviebuzz.fragments.CustomListFragment;
import net.toracode.moviebuzz.fragments.ReviewFragment;


/*
Expects a bundle with a string attribute 'ref'. Bundle can have more attributes that will be passed to the fragment
 */
public class ListFragmentsActivity extends AppCompatActivity implements View.OnClickListener {

    private final String REF_CUSTOMLIST_ADAPER_COMMENT_BUTTON = "CustomListAdapterCommentButton";
    private final String REF_MY_LIST = "MyList";
    private final String REF_MY_FOLLOWING_LIST = "MyFollowingList";
    private final String REF_MY_REVIEWS = "MyReviews";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_fragments);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // FAB Actions
        FloatingActionButton fab = (FloatingActionButton) this.findViewById(R.id.fab);
        fab.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            switch (bundle.getString("ref")) {
                case REF_CUSTOMLIST_ADAPER_COMMENT_BUTTON: // if this activity opens from the comment button click on custom list adapter.
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle("Comments");
                    this.setupCommentsFragment(bundle);  // Replaces comment fragment
                    break;
                case REF_MY_LIST:  // opens clicking on mylist on navigation drawer.
                    if (getSupportActionBar() != null) {
                        if (!bundle.getBoolean("isPublic"))
                            getSupportActionBar().setTitle("My List");
                    }
                    this.setupCustomListFragment(bundle);  // Replaces Custom List Fragment
                    fab.setVisibility(View.VISIBLE);
                    break;
                case REF_MY_FOLLOWING_LIST:
                    if (getSupportActionBar() != null) {
                        if (bundle.getBoolean("isFollowing"))
                            getSupportActionBar().setTitle("My Following List");
                    }
                    this.setupCustomListFragment(bundle);  // Replaces Custom List Fragment
                    break;
                case REF_MY_REVIEWS:
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle("My Reviews");
                    this.setupReviewsFragment(bundle);  // Replaces Review fragment
                    break;
            }

        }
    }

    private void setupReviewsFragment(Bundle bundle) {
        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, reviewFragment, "ReviewFragment").commit();
    }

    private void setupCustomListFragment(Bundle bundle) {
        CustomListFragment fragment = new CustomListFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment, "CustomListFragment").commit();
    }

    private void setupCommentsFragment(Bundle bundle) {
        CommentsFragment commentsFragment = new CommentsFragment();
        commentsFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, commentsFragment, "CommentsFragment").commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            this.finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fab)
            new CustomListOperations(this).showNewListDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
