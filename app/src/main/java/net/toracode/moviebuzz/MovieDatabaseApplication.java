package net.toracode.moviebuzz;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.accountkit.AccountKit;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by sayemkcn on 10/25/16.
 */

public class MovieDatabaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        AccountKit.initialize(getApplicationContext());

    }
}
