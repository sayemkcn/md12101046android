package net.toracode.moviedb;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.accountkit.AccountKit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.toracode.moviedb.adapters.CustomListAdapter;
import net.toracode.moviedb.entity.CustomList;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

public class MyListActivity extends AppCompatActivity {

    @BindView(R.id.myListRecyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        this.fetchMyLists();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewListDialog();
            }
        });
    }


    private void fetchMyLists() {
        if (AccountKit.getCurrentAccessToken() != null) {
            String accountId = AccountKit.getCurrentAccessToken().getAccountId();
            final String url = getResources().getString(R.string.baseUrl) + "list?accountId=" + accountId;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Response response = new ResourceProvider(MyListActivity.this).fetchGetResponse(url);
                        final String responseBody = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.code() == ResourceProvider.RESPONSE_CODE_FOUND) {
                                    List<CustomList> listOfCustomList = parseCustomList(responseBody);
                                    setUpCustomListRecyclerView(recyclerView, listOfCustomList);
                                }
                            }
                        });

                    } catch (IOException e) {
                        Log.e("GET_LISTS", e.toString());
                    }
                }
            }).start();
        }
    }

    private List<CustomList> parseCustomList(String jsonArrayString) {
        // Creates the json object which will manage the information received
        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        Gson gson = builder.create();
        List<CustomList> listOfCustomList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for (int i = 0; i < jsonArray.length(); i++) {
                CustomList list = gson.fromJson(jsonArray.getJSONObject(i).toString(), CustomList.class);
                listOfCustomList.add(list);
            }
        } catch (JSONException e) {
            Log.e("LIST_JSON_PERSON", e.toString());
        }
        return listOfCustomList;
    }

    private void setUpCustomListRecyclerView(RecyclerView recyclerView, List<CustomList> listOfCustomList) {
        recyclerView.setAdapter(new CustomListAdapter(this, listOfCustomList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
//        this.recyclerView.setVisibility(View.VISIBLE);
    }

    // create a custom list dialog.
    private void showNewListDialog() {
        new MaterialDialog.Builder(this)
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
            this.startActivity(new Intent(this, PreferenceActivity.class));
            return;
        }

        String accountId = AccountKit.getCurrentAccessToken().getAccountId();
        final String url = getResources().getString(R.string.baseUrl) + "list/create?accountId=" + accountId + "&title=" + name + "&description=" + desc + "&type=" + type;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(MyListActivity.this).fetchPostResponse(url);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_INTERNAL_SERVER_ERROR) {
                                Commons.showSimpleToast(getApplicationContext(), "Can not create list!");
                            } else if (response.code() == ResourceProvider.RESPONSE_NOT_ACCEPTABLE) {
                                Commons.showDialog(MyListActivity.this, "Can not create list!", "1. You must enter a name (length should be at least three letters)\n" +
                                        "2. You must enter a type. Type can be anything you want but if it's \"public\" the list will be shown to all.");
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_CREATED) {
                                String message;
                                if (name.toLowerCase().equals("watchlist")) {
                                    message = "We have created an watchlist for you. You can create new list of your own by clicking new list button on the dialog.";
                                } else {
                                    message = "Your list has been created successfully.";
                                }
                                Commons.showDialog(MyListActivity.this, "Successfull!", message);
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
