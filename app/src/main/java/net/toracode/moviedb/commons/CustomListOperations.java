package net.toracode.moviedb.commons;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.accountkit.AccountKit;

import net.toracode.moviedb.PreferenceActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by sayemkcn on 11/17/16.
 */

public class CustomListOperations {
    private Activity context;

    public CustomListOperations(Activity context) {
        this.context = context;
    }

    // create a custom list dialog.
    public void showNewListDialog() {
        new MaterialDialog.Builder(this.context)
                .title("Create new list")
                .customView(R.layout.create_custom_list, true)
                .positiveText("Create")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        EditText nameEditText = (EditText) view.findViewById(R.id.listName);
                        EditText descEditText = (EditText) view.findViewById(R.id.listDescription);
                        EditText typeEditText = (EditText) view.findViewById(R.id.listType);
                        String name = nameEditText.getText().toString();
                        String desc = descEditText.getText().toString();
                        String type = typeEditText.getText().toString();
//
                        createCustomList(name, desc, type);
                    }
                })
                .negativeText("Cancel")
                .canceledOnTouchOutside(false)
                .show();

    }

    private void createCustomList(final String name, String desc, String type) {
        if (AccountKit.getCurrentAccessToken() == null) {
            this.context.startActivity(new Intent(this.context, PreferenceActivity.class));
            return;
        }

        String accountId = AccountKit.getCurrentAccessToken().getAccountId();
        final String url = this.context.getResources().getString(R.string.baseUrl) + "list/create?accountId=" + accountId + "&title=" + name + "&description=" + desc + "&type=" + type;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(context).fetchPostResponse(url);
                    if (context != null)
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.code() == ResourceProvider.RESPONSE_CODE_INTERNAL_SERVER_ERROR) {
                                    Commons.showSimpleToast(context.getApplicationContext(), "Can not create list!");
                                } else if (response.code() == ResourceProvider.RESPONSE_NOT_ACCEPTABLE) {
                                    Commons.showDialog(context, "Can not create list!", "1. You must enter a name (length should be at least three letters)\n" +
                                            "2. You must enter a type. Type can be anything you want but if it's \"public\" the list will be shown to all.");
                                } else if (response.code() == ResourceProvider.RESPONSE_CODE_CREATED) {
                                    String message;
                                    if (name.toLowerCase().equals("watchlist")) {
                                        message = "We have created an watchlist for you. You can create new list of your own by clicking new list button on the dialog.";
                                    } else {
                                        message = "Your list has been created successfully.";
                                    }
                                    Commons.showDialog(context, "Successfull!", message);
                                }
                            }
                        });

                } catch (IOException e) {
                    Log.e("CREATE_CUSTOM_LIST", e.toString());
                }
            }
        }).start();

    }
}
