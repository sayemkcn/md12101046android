package net.toracode.moviebuzz.commons;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.accountkit.AccountKit;

import net.toracode.moviebuzz.PreferenceActivity;
import net.toracode.moviebuzz.R;
import net.toracode.moviebuzz.entity.CustomList;
import net.toracode.moviebuzz.service.Commons;
import net.toracode.moviebuzz.service.ResourceProvider;

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
                        Spinner typeSpinner = (Spinner) view.findViewById(R.id.listType);
                        String name = nameEditText.getText().toString();
                        String desc = descEditText.getText().toString();
                        String type = typeSpinner.getSelectedItem().toString();
//
                        createCustomList(name, desc, type);
                    }
                })
                .negativeText("Cancel")
                .canceledOnTouchOutside(false)
                .show();

    }

    public void createCustomList(final String name, String desc, String type) {
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


    public void showEditListDialog(Button view, final CustomList list, final String accountId) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("Edit list")
                .customView(R.layout.create_custom_list, true)
                .positiveText("Submit")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        EditText nameEditText = (EditText) view.findViewById(R.id.listName);
                        EditText descEditText = (EditText) view.findViewById(R.id.listDescription);
                        Spinner typeSpinner = (Spinner) view.findViewById(R.id.listType);
                        String name = nameEditText.getText().toString();
                        String desc = descEditText.getText().toString();
                        String type = typeSpinner.getSelectedItem().toString();
//
                        submitCustomList(list, accountId, name, desc, type);
                    }
                })
                .negativeText("Cancel")
                .canceledOnTouchOutside(false)
                .build();
        EditText nameEditText = (EditText) dialog.getCustomView().findViewById(R.id.listName);
        EditText descEditText = (EditText) dialog.getCustomView().findViewById(R.id.listDescription);
        Spinner typeSpinner = (Spinner) dialog.getCustomView().findViewById(R.id.listType);
        nameEditText.setText(list.getTitle());
        descEditText.setText(list.getDescription());
        if (list.getType().toLowerCase().equals("private"))
            typeSpinner.setSelection(0);
        else if (list.getType().toLowerCase().equals("public"))
            typeSpinner.setSelection(1);
        dialog.show();
    }

    // post edited list to server
    private void submitCustomList(CustomList list, String accountId, final String name, String desc, String type) {
        if (AccountKit.getCurrentAccessToken() == null) {
            context.startActivity(new Intent(context, PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return;
        }
        final String url = context.getResources().getString(R.string.baseUrl) + "list/edit/" + list.getUniqueId() + "?title="
                + name + "&description=" + desc + "&type=" + type + "&accountId=" + accountId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(context).fetchPostResponse(url);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_INTERNAL_SERVER_ERROR) {
                                Commons.showSimpleToast(context, "Can not create list!");
                            } else if (response.code() == ResourceProvider.RESPONSE_NOT_ACCEPTABLE) {
                                Commons.showDialog(context, "Can not create list!", "1. You must enter a name (length should be at least three characters)\n" +
                                        "2. You must enter a type. Type can be anything you want but if it's \"public\" the list will be shown to all.");
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_CREATED) {
                                String message = "Your list has been created successfully.";
                                Commons.showDialog(context, "Successful!", message);
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
