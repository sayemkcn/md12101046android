package net.toracode.moviebuzz.security;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;

/**
 * Created by sayemkcn on 12/4/16.
 */

public class Auth {

    public static boolean isLoggedIn() {
        return AccountKit.getCurrentAccessToken() != null;
    }

    public static String getAccountId() {
        AccessToken accessToken = AccountKit.getCurrentAccessToken();
        if (accessToken != null)
            return accessToken.getAccountId();
        return null;
    }

}
