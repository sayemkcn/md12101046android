package net.toracode.moviedb.service;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by sayemkcn on 8/10/16.
 */
public class ResourceProvider {

    private Activity context;
    private String response = null;

    public static final int RESPONSE_CODE_CREATED = 201;
    public static final int RESPONSE_CODE_FOUND = 302;

    public ResourceProvider(Activity context) {
        this.context = context;
    }

    public String fetchData(String url) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body().string();

    }

    public Response fetchPostResponse(String url) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("test","test");
        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .method("POST", RequestBody.create(null, new byte[0]))
                .post(requestBody)
                .build();
        Log.d("FUCKEN_URL",request.url().toString());
        return new OkHttpClient().newCall(request).execute();
    }

}
