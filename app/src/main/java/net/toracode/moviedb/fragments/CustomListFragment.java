package net.toracode.moviedb.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import net.toracode.moviedb.PreferenceActivity;
import net.toracode.moviedb.R;
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

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by sayemkcn on 11/15/16.
 */
/*
 This fragment expects a bundle that has a boolean attribute isPublic. true for public lists and false for private.
  */
public class CustomListFragment extends Fragment {

    private int page = 0;
    private RecyclerView customListRecyclerView;

    private boolean isPublic = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.isPublic = getArguments().getBoolean("isPublic");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.customListRecyclerView = (RecyclerView) getView().findViewById(R.id.customListRecyclerView);

        // if private and not logged in.
        if (!isPublic && AccountKit.getCurrentAccessToken() == null) {
            getActivity().startActivity(new Intent(getActivity(), PreferenceActivity.class));
            return;
        }
        // build the url
        String url = this.buildUrl(this.isPublic);
        this.fetchCustomLists(url);
    }

    private String buildUrl(boolean isPublic) {
        String url = null;
        if (!isPublic) {
            if (AccountKit.getCurrentAccessToken() != null) {
                String accountId = AccountKit.getCurrentAccessToken().getAccountId();
                url = getResources().getString(R.string.baseUrl) + "list?accountId=" + accountId;
            }
        } else {
            url = getResources().getString(R.string.baseUrl) + "list/public?page=" + this.page;
        }
        return url;
    }

    private void fetchCustomLists(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(getActivity()).fetchGetResponse(url);
                    ResponseBody responseBody = response.body();
                    final String responseBodyString = responseBody.string();
                    responseBody.close(); // close connection
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.code() == ResourceProvider.RESPONSE_CODE_FOUND) {
                                    List<CustomList> listOfCustomList = parseCustomList(responseBodyString);
                                    setUpCustomListRecyclerView(customListRecyclerView, listOfCustomList);
                                }
                            }
                        });

                } catch (IOException e) {
                    Log.e("GET_LISTS", e.toString());
                }
            }
        }).start();
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
        recyclerView.setAdapter(new CustomListAdapter(getActivity(), listOfCustomList));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setNestedScrollingEnabled(false);
//        this.recyclerView.setVisibility(View.VISIBLE);
    }

}
