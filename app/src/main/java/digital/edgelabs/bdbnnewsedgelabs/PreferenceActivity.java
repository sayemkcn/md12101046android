package digital.edgelabs.bdbnnewsedgelabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.Commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.adapters.PreferenceAdapter;
import digital.edgelabs.bdbnnewsedgelabs.entity.PreferenceSingleItem;
import digital.edgelabs.bdbnnewsedgelabs.events.PrefChangeEvent;

public class PreferenceActivity extends AppCompatActivity {

    @BindView(R.id.preferenceRecyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        // setup recycler view
        this.recyclerView.setAdapter(new PreferenceAdapter(this, this.getPreferenceItemList()));
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @NonNull
    private List<PreferenceSingleItem> getPreferenceItemList() {
        String[] sourceNames = getResources().getStringArray(R.array.sourceNames);
        int[] sourceIds = getResources().getIntArray(R.array.sourceIds);
        List<PreferenceSingleItem> prefItemList = new ArrayList();
        for (int i = 0; i < sourceNames.length && i < sourceIds.length; i++) {
            PreferenceSingleItem prefItem = new PreferenceSingleItem();
            prefItem.setName(sourceNames[i]);
            if (Pref.isNull(this, "source" + (i + 1))) {
                prefItem.setActivated(true);
            } else {
                prefItem.setActivated(Pref.getPreference(this, "source" + (i + 1)));
            }
            prefItemList.add(prefItem);
        }
        return prefItemList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            this.finish();
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPrefChange(PrefChangeEvent event) {
        Pref.savePreference(this, "source" + event.getSourceId(), event.isChecked());
    }
}
