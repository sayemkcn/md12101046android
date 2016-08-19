package digital.edgelabs.bdbnnewsedgelabs.service;

import android.app.Activity;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import digital.edgelabs.bdbnnewsedgelabs.entity.CategoryEntity;
import digital.edgelabs.bdbnnewsedgelabs.events.UserCategoryLoadEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by SAyEM on 19-Aug-16.
 */
public class Commons {
    public static void loadUserCategoryList(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();

                    JSONArray jsonArray = new JSONArray(response.body().string());
                    final List<CategoryEntity> categoryList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        CategoryEntity category = new CategoryEntity();
                        category.setId(jsonObject.getLong("categoryId"));
                        category.setName(jsonObject.getString("categoryName"));
                        category.setIconUrl(jsonObject.getString("iconUrl"));
                        category.setAccentColorCode(jsonObject.getString("accentColorCode"));
                        categoryList.add(category);
                    }
                    EventBus.getDefault().post(new UserCategoryLoadEvent(categoryList));
                } catch (JSONException e) {
                    Log.e("JSON_EX_CAT_LIST", e.toString());
                } catch (IOException e) {
                    Log.e("JSON_IOE", e.toString());
                }
            }
        }).start();

    }
}
