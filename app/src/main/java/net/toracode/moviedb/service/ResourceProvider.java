package net.toracode.moviedb.service;

import android.app.Activity;


import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by sayemkcn on 8/10/16.
 */
public class ResourceProvider {

    private Activity context;
    private String response = null;

    public ResourceProvider(Activity context) {
        this.context = context;
    }

    public String fetchData(String url) throws IOException {
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
////                EventBus.getDefault().post(new UserCategoryLoadEvent(response));
//                ResourceProvider.this.response = response;
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
