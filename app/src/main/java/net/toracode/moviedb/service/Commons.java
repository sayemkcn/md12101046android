package net.toracode.moviedb.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.toracode.moviedb.DetailsActivity;
import net.toracode.moviedb.OfflineActivity;
import net.toracode.moviedb.PreferenceActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.CategoryEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by SAyEM on 19-Aug-16.
 */
public class Commons {
//
//    public static Map<TimeUnit, Long> computeTimeDiff(Date date1, Date date2) {
//        long diffInMillies = date2.getTime() - date1.getTime();
//        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
//        Collections.reverse(units);
//        Map<TimeUnit, Long> result = new LinkedHashMap<TimeUnit, Long>();
//        long milliesRest = diffInMillies;
//        for (TimeUnit unit : units) {
//            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
//            long diffInMilliesForUnit = unit.toMillis(diff);
//            milliesRest = milliesRest - diffInMilliesForUnit;
//            result.put(unit, diff);
//        }
//        return result;
//    }

    public static void showDialog(final Activity context, String title, String message) {
        final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(context);

        int dividerColor = context.getResources().getColor(R.color.colorPrimaryDark);
        int dialogColor = context.getResources().getColor(R.color.colorPrimary);
        int buttonBackgroundColor = context.getResources().getColor(R.color.colorAccent);
        Effectstype effect = Effectstype.Fall;

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
                if (context instanceof PreferenceActivity)
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
                        context.startActivity(new Intent(context, OfflineActivity.class));
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
            Log.i("BadWindowTokenEx", "Nifty Dialog bad window token exception on MainFragment");
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

    public static Gson buildGson() {
        // Creates the json object which will manage the information received
        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        return builder.create();
    }
}
