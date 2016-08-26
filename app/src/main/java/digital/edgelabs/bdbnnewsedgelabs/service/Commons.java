package digital.edgelabs.bdbnnewsedgelabs.service;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import digital.edgelabs.bdbnnewsedgelabs.DetailsActivity;
import digital.edgelabs.bdbnnewsedgelabs.R;
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

    public static Map<TimeUnit, Long> computeTimeDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);
        Map<TimeUnit, Long> result = new LinkedHashMap<TimeUnit, Long>();
        long milliesRest = diffInMillies;
        for (TimeUnit unit : units) {
            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit, diff);
        }
        return result;
    }

    public static void showDialog(final Activity context, String title, String message) {
        final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(context);

        int dividerColor = context.getResources().getColor(R.color.colorPrimaryDark);
        int dialogColor = context.getResources().getColor(R.color.colorPrimary);
        int buttonBackgroundColor = context.getResources().getColor(R.color.colorAccent);
        Effectstype effect = Effectstype.Shake;

        dialogBuilder
                .withTitle(title)
                .withTitleColor(context.getResources().getColor(android.R.color.white))
                .withDividerColor(dividerColor)
                .withMessage(message)
                .withMessageColor(context.getResources().getColor(android.R.color.white))
                .withDialogColor(dialogColor)
                .withEffect(effect)
                .withDuration(1000)
                .withIcon(R.mipmap.ic_launcher)
                .withButton1Text("Okay")
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.cancel();
                        if (!(context instanceof DetailsActivity))
                            context.finish();
                    }
                })
                .isCancelableOnTouchOutside(false)
                .show();

    }

    public static SuperToast getLoadingToast(Context context) {
        SuperToast toast = SuperActivityToast.create(context, new Style(), Style.TYPE_PROGRESS_BAR)
                .setProgressBarColor(Color.WHITE)
                .setText(context.getResources().getString(R.string.message_loading))
                .setDuration(Style.DURATION_LONG)
                .setFrame(Style.FRAME_KITKAT)
                .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_TEAL))
                .setAnimations(Style.ANIMATIONS_FLY);
        return toast;
    }
}
