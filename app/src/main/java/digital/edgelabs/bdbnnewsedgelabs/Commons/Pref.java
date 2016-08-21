package digital.edgelabs.bdbnnewsedgelabs.Commons;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sayemkcn on 8/22/16.
 */
public class Pref {
    public static String PREF_NAME = "BDBNNEWSEDGELABS";

    public static void savePreference(Context context, String key, boolean value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();

        prefEditor.putBoolean(key, value);
        prefEditor.apply();

    }

    public static boolean getPreference(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, true);
    }

    public static boolean isNull(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (sharedPref.contains(key))
            return false;
        return true;
    }

}
