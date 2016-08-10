package digital.edgelabs.bdbnnewsedgelabs.fragmenthelpers;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.events.NewsFetchEvent;
import digital.edgelabs.bdbnnewsedgelabs.service.NewsProvider;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class MainFragmentHelper {
    private Activity context;
    private View rootView;

    private TextView textView;

    public MainFragmentHelper(Activity context, View rootView) {
        this.context = context;
        this.rootView = rootView;

        // register eventbus
        EventBus.getDefault().register(this);
    }

    public void exec(){
        this.textView = (TextView) rootView.findViewById(R.id.textView);

        new NewsProvider(context).fetchNews(context.getResources().getString(R.string.newsUrl));

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewsFetched(NewsFetchEvent newsFetchEvent) {
        this.textView.setText(newsFetchEvent.getResponse());
    }
}
