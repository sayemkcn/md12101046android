package net.toracode.moviedb.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

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

import net.toracode.moviedb.DetailsActivity;
import net.toracode.moviedb.OfflineNewsActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.CategoryEntity;
import net.toracode.moviedb.events.UserCategoryLoadEvent;
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

                    List<CategoryEntity> categoryList = parseJson(response.body().string());
//                    JSONArray jsonArray = new JSONArray(response.body().string());
//                    final List<CategoryEntity> categoryList = new ArrayList<>();
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        CategoryEntity category = new CategoryEntity();
//                        category.setId(jsonObject.getLong("categoryId"));
//                        category.setName(jsonObject.getString("categoryName"));
//                        category.setIconUrl(jsonObject.getString("iconUrl"));
//                        category.setAccentColorCode(jsonObject.getString("accentColorCode"));
//                        categoryList.add(category);
//                    }
                    if (categoryList != null && categoryList.size() != 0)
                        EventBus.getDefault().post(new UserCategoryLoadEvent(categoryList));
                } catch (IOException e) {
                    Log.e("JSON_IOE", e.toString());
                }
            }
        }).start();

    }

    public static void loadUserCategoryListFromResource(String categoryJson) {
        final List<CategoryEntity> categoryList = parseJson(categoryJson);
        if (categoryList != null && categoryList.size() != 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new UserCategoryLoadEvent(categoryList));
                }
            }, 100);
        }
    }

    private static List<CategoryEntity> parseJson(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<CategoryEntity> categoryList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                CategoryEntity category = new CategoryEntity();
                category.setId(jsonObject.getLong("categoryId"));
                category.setName(jsonObject.getString("categoryName"));
                category.setIconUrl(jsonObject.getString("iconUrl"));
                category.setAccentColorCode(jsonObject.getString("accentColorCode"));
                categoryList.add(category);
            }
            return categoryList;
        } catch (JSONException e) {
            Log.d("CAT_JSON_EX", e.toString());
        }
        return null;
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

        final NiftyDialogBuilder builder = dialogBuilder
                .withTitle(title)
                .withTitleColor(context.getResources().getColor(android.R.color.white))
                .withDividerColor(dividerColor)
                .withMessage(message)
                .withMessageColor(context.getResources().getColor(android.R.color.white))
                .withDialogColor(dialogColor)
                .withEffect(effect)
                .withDuration(1000)
                .withIcon(R.mipmap.ic_launcher);
        builder.withButton1Text("Okay");
        builder.setButton1Click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (builder.isShowing())
                    builder.cancel();
                if (!(context instanceof DetailsActivity))
                    context.finish();
            }
        });
        builder.isCancelableOnTouchOutside(false);
        builder.show();

    }

    public static void showNetworkUnavailableDialog(final Activity context, String title, String message) {
        try {
            final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(context);

            int dividerColor = context.getResources().getColor(R.color.colorPrimaryDark);
            int dialogColor = context.getResources().getColor(R.color.colorPrimary);
            int buttonBackgroundColor = context.getResources().getColor(R.color.colorAccent);
            Effectstype effect = Effectstype.Shake;

            final NiftyDialogBuilder builder = dialogBuilder
                    .withTitle(title)
                    .withTitleColor(context.getResources().getColor(android.R.color.white))
                    .withDividerColor(dividerColor)
                    .withMessage(message)
                    .withMessageColor(context.getResources().getColor(android.R.color.white))
                    .withDialogColor(dialogColor)
                    .withEffect(effect)
                    .withDuration(1000)
                    .withIcon(R.mipmap.ic_launcher);
            builder.withButton2Text("Yes");
            builder.setButton2Click(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (builder.isShowing())
                        builder.cancel();
                    if (!(context instanceof DetailsActivity)) {
                        context.finish();
                        context.startActivity(new Intent(context, OfflineNewsActivity.class));
                    }

                }
            });
            builder.withButton1Text("No");
            builder.setButton1Click(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (builder.isShowing())
                        builder.cancel();
                    if (!(context instanceof DetailsActivity))
                        context.finish();
                }
            });
            builder.isCancelableOnTouchOutside(false);
            builder.show();
        } catch (WindowManager.BadTokenException e) {
            Log.i("BadWindowTokenEx","Nifty Dialog bad window token exception on MainFragment");
        }
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

    public static void showSimpleToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.getView().setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        toast.getView().setPadding(20, 20, 20, 20);
        toast.show();
    }

    public static void share(Activity context, String title, String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, title));
    }
    public static void showDevDialog(final Activity context) {
        new AlertDialog.Builder(context)
                .setTitle("Dev")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Sayem Hossain")
                .setPositiveButton("OK", null)
                .setNegativeButton("CONTACT DEV", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/SAyEM.RimOn"));
                        context.startActivity(browserIntent);
                    }
                }).show();
    }
}