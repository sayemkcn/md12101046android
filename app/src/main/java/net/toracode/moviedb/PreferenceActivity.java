package net.toracode.moviedb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.toracode.moviedb.commons.Pref;
import net.toracode.moviedb.entity.User;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

import static net.toracode.moviedb.MainActivity.APP_REQUEST_CODE;

public class PreferenceActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.nameEditText)
    EditText nameEditText;
    @BindView(R.id.emailEditText)
    EditText emailEditText;
    @BindView(R.id.submitButton)
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null)
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        // FACEBOOK ACCOUNT KIT
        AccessToken accessToken = AccountKit.getCurrentAccessToken();

        if (accessToken != null) {
            //Handle Returning User
            this.fillUserData(accessToken);
            Log.d("ACCOUNT_KIT", "LoggedIn");
        } else {
            //Handle new or logged out user
            this.onLoginPhone();
        }

        this.submitButton.setOnClickListener(this);

    }

    private void fillUserData(AccessToken accessToken) {
        final SuperToast loadingToast = Commons.getLoadingToast(this);
        loadingToast.show();
        final String url = getResources().getString(R.string.baseUrl) + "user/" + accessToken.getAccountId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = new ResourceProvider(PreferenceActivity.this).fetchPostResponse(url);
                    if (response.code() == ResourceProvider.RESPONSE_CODE_FOUND) {
                        final User user = parseUserObject(response.body().string());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (loadingToast.isShowing()) loadingToast.dismiss();
                                nameEditText.setText(user.getName());
                                emailEditText.setText(user.getEmail());
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.d("FILL_DATA", e.toString());
                }
            }
        }).start();
    }

    private User parseUserObject(String userJsonString) {
        // Creates the json object which will manage the information received
        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        Gson gson = builder.create();
        User user = gson.fromJson(userJsonString, User.class);
        return user;
    }


    // *******ACCOUNT KIT FACEBOOK *******//
    public void onLoginPhone() {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

            if (loginResult.getError() != null) {
                Commons.showSimpleToast(this, loginResult.getError().getErrorType().getMessage());
//                showErrorActivity(loginResult.getError());
            } else if (loginResult.wasCancelled()) {
                Commons.showSimpleToast(getApplicationContext(), "Not cool man! not cool!");
                this.finish();
            } else {
                if (loginResult.getAccessToken() != null) {
                    //**********LOGGED IN********//
                    // SEND REQUEST WITH THIS ACCOUNT ID //
                    registerUser(loginResult);
                    this.fillUserData(loginResult.getAccessToken());
//                    Log.d("AUTH_TOKEN", loginResult.getAccessToken().getAccountId());
                } else {
                    Commons.showSimpleToast(getApplicationContext(), "Success:%s..." +
                            loginResult.getAuthorizationCode().substring(0, 10));
                    Log.d("AUTH_CODE", loginResult.getAuthorizationCode());

                }

                // If you have an authorization code, retrieve it from
                // loginResult.getAuthorizationCode()
                // and pass it to your server and exchange it for an access token.

                // Success! Start your next activity...
//                goToMyLoggedInActivity();
            }

        }
    }

    // registers user with account kit account id
    private void registerUser(final AccountKitLoginResult loginResult) {
        final String url = getResources().getString(R.string.baseUrl) + "user/"
                + loginResult.getAccessToken().getAccountId() + "?name=&email=";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(PreferenceActivity.this).fetchPostResponse(url);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_CREATED) {
                                Pref.savePreference(PreferenceActivity.this, Pref.PREF_ACCOUNT_ID, loginResult.getAccessToken().getAccountId());
                                Commons.showSimpleToast(getApplicationContext(), "Registration successful!!");
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_FOUND) {
                                Pref.savePreference(PreferenceActivity.this, Pref.PREF_ACCOUNT_ID, loginResult.getAccessToken().getAccountId());
                                Commons.showSimpleToast(getApplicationContext(), "Registration successful!!");
                            }
                        }
                    });
                    Log.d("RESPONSE", response.toString());
                } catch (IOException e) {
                    Log.e("EXCEPTION", e.toString());
                }
            }
        }).start();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            this.finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.submitButton) {
            if (AccountKit.getCurrentAccessToken() == null) {
                this.onLoginPhone();
            } else {
                this.postEditedData();
                this.updatePhoneNumber();
            }
        }
    }

    private void updatePhoneNumber() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                String accountId = AccountKit.getCurrentAccessToken().getAccountId();
                String phone = account.getPhoneNumber().toString();
                String url = getResources().getString(R.string.baseUrl) + "user/update/" + accountId + "/" + phone;
                try {
                    Response response = new ResourceProvider(PreferenceActivity.this).fetchPostResponse(url);
                    Log.i("PHONE_RESPONSE_CODE", response.code() + "");
                } catch (IOException e) {
                    Log.e("UPDATE_PHONE", e.toString());
                }
            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.e("UPDATE_PHONE", accountKitError.toString());
            }
        });

    }

    private void postEditedData() {
        String name = this.nameEditText.getText().toString();
        String email = this.emailEditText.getText().toString();
        if (name.isEmpty() || name.length() < 2) {
            this.nameEditText.setError("You must enter your name!");
            return;
        }
        if (email.isEmpty() || email.split("@").length < 2) {
            this.emailEditText.setError("This is not a valid email address!");
            return;
        }
        final SuperToast loadingToast = Commons.getLoadingToast(this);
        loadingToast.show();
        final String url = getResources().getString(R.string.baseUrl) + "user/update/" + AccountKit.getCurrentAccessToken().getAccountId() + "?name=" + name + "&email=" + email;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(PreferenceActivity.this).fetchPostResponse(url);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (loadingToast.isShowing()) loadingToast.dismiss();
                            if (response.code() == ResourceProvider.RESPONSE_ACCEPTED) {
                                Commons.showDialog(PreferenceActivity.this, "Successful!", "Successfully updated your informations.");
                            } else {
                                Commons.showDialog(PreferenceActivity.this, "We are sorry!", "Something happened. So we're unable to update your informations right now. Can you please try again later?");
                            }
                        }
                    });

                } catch (IOException e) {
                    Log.e("POST_EDIT_DATA", e.toString());
                }
            }
        }).start();
    }
}
