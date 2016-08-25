package digital.edgelabs.bdbnnewsedgelabs.Commons;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sayemkcn on 8/22/16.
 */
public class Pref {
    public static final String PREF_NAME = "BDBNNEWSEDGELABS";
    public static final String PREF_SIZE = "prefSize";
    public static final String PREF_KEY_BOOKMARK_LIST = "bookmark_list";

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

    public static void savePreference(Context context, String key, int value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();

        prefEditor.putInt(key, value);
        prefEditor.apply();

    }

    public static int getPreferenceInt(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getInt(key,0);
    }

    public static void savePreference(Context context,String key,String value){
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();

        prefEditor.putString(key, value);
        prefEditor.apply();
    }
    public static String getPreferenceString(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(key,"");
    }


    public static boolean isNull(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (sharedPref.contains(key))
            return false;
        return true;
    }

}
