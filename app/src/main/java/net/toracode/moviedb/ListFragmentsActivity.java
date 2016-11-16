package net.toracode.moviedb;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.accountkit.AccountKit;

import net.toracode.moviedb.commons.CustomListOperations;
import net.toracode.moviedb.fragments.CommentsFragment;
import net.toracode.moviedb.fragments.CustomListFragment;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import java.io.IOException;

import okhttp3.Response;


/*
Expects a bundle with a string attribute 'ref'. Bundle can have more attributes that will be passed to the fragment
 */
public class ListFragmentsActivity extends AppCompatActivity implements View.OnClickListener{

    private final String REF_CUSTOMLIST_ADAPER_COMMENT_BUTTON = "CustomListAdapterCommentButton";
    private final String REF_MY_LIST = "MyList";

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
            }

        }
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
        if (id==R.id.fab)
            new CustomListOperations(this).showNewListDialog();
    }
}
