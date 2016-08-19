package digital.edgelabs.bdbnnewsedgelabs.service;

import android.app.Activity;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import digital.edgelabs.bdbnnewsedgelabs.events.NewsFetchEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by sayemkcn on 8/10/16.
 */
public class NewsProvider {

    private Activity context;
    private String response = null;

    public NewsProvider(Activity context) {
        this.context = context;
    }

    public String fetchNews(String url) throws IOException {
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
////                EventBus.getDefault().post(new NewsFetchEvent(response));
//                NewsProvider.this.response = response;
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        });
//        Volley.newRequestQueue(this.context).add(stringRequest);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body().string();

    }

}
