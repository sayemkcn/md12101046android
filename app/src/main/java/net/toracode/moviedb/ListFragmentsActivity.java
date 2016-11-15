package net.toracode.moviedb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.toracode.moviedb.fragmants.CommentsFragment;

public class ListFragmentsActivity extends AppCompatActivity {

    private final String REF_COMMENT_ADAPER = "CustomListAdapter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_fragments);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            if (bundle.getString("ref").equals(REF_COMMENT_ADAPER)) {
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle("Comments");
                this.setupCommentsFragment(bundle);
            }
        }
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
}
