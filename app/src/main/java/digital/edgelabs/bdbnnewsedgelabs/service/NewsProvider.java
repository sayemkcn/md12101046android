package digital.edgelabs.bdbnnewsedgelabs.service;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import digital.edgelabs.bdbnnewsedgelabs.events.NewsFetchEvent;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class NewsProvider {

    private Activity context;

    public NewsProvider(Activity context) {
        this.context = context;
    }

    public void fetchNews(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                EventBus.getDefault().post(new NewsFetchEvent(response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        Volley.newRequestQueue(this.context).add(stringRequest);
    }

}
