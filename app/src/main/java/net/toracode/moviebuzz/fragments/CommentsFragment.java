package net.toracode.moviebuzz.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.accountkit.AccountKit;
import com.google.gson.Gson;

import net.toracode.moviebuzz.PreferenceActivity;
import net.toracode.moviebuzz.R;
import net.toracode.moviebuzz.adapters.CommentsAdapter;
import net.toracode.moviebuzz.entity.Comment;
import net.toracode.moviebuzz.service.Commons;
import net.toracode.moviebuzz.service.ResourceProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by sayemkcn on 11/15/16.
 */

public class CommentsFragment extends Fragment implements View.OnClickListener {

    private RecyclerView commentsRecyclerView;
    private EditText commentBoxEditText;
    private TextView noCommentTextView;

    private Long listId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.listId = getArguments().getLong("listId");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.comments_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.commentsRecyclerView = (RecyclerView) getView().findViewById(R.id.commentsRecyclerView);
        this.commentBoxEditText = (EditText) getView().findViewById(R.id.commentBoxEditText);
        this.noCommentTextView = (TextView) getView().findViewById(R.id.noCommentTextView);

        Button postCommentButton = (Button) getView().findViewById(R.id.postCommentButton);

        postCommentButton.setOnClickListener(this);

        if (this.listId != null)
            this.fetchComments(this.listId);
    }

    private void fetchComments(Long listId) {
        final String url = getResources().getString(R.string.baseUrl) + "comment/all?listId=" + listId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(getActivity()).fetchGetResponse(url);
                    ResponseBody responseBody = response.body();
                    final String responseBodyString = responseBody.string();
                    responseBody.close(); // Look Trump! I haven't forgot to close the fucking connection.
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.code() == ResourceProvider.RESPONSE_CODE_OK) {
                                    List<Comment> commentList = parseComments(responseBodyString);
                                    setupRecyclerView(commentList);
                                }
                            }
                        });
                } catch (IOException e) {
                    Log.e("FETCH_COMMENTS", e.toString());
                }
            }
        }).start();
    }

    private void setupRecyclerView(List<Comment> commentList) {
        this.noCommentTextView.setVisibility(View.GONE);
        this.commentsRecyclerView.setVisibility(View.VISIBLE);
        Collections.reverse(commentList);
        this.commentsRecyclerView.setAdapter(new CommentsAdapter(getActivity(), commentList));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        this.commentsRecyclerView.setLayoutManager(layoutManager);
    }

    private List<Comment> parseComments(String responseBodyString) {
        List<Comment> commentList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseBodyString);
            for (int i = 0; i < jsonArray.length(); i++) {
                Gson gson = Commons.buildGson();
                Comment comment = gson.fromJson(jsonArray.getJSONObject(i).toString(), Comment.class);
                commentList.add(comment);
            }
        } catch (JSONException e) {
            Log.e("PARSE_COMMENT_JSON", e.toString());
        }
        return commentList;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.postCommentButton) {
            if (AccountKit.getCurrentAccessToken() == null) {
                this.startActivity(new Intent(getActivity(), PreferenceActivity.class));
                return;
            }
            this.postComment();
        }
    }

    private void postComment() {
        if (!Commons.isNetworkAvailable(getActivity())) {
            Commons.showSimpleToast(getActivity().getApplicationContext(), "Please connect to the internet first!");
            return;
        }
        String comment = this.commentBoxEditText.getText().toString();
        if (comment.isEmpty()) {
            this.commentBoxEditText.setError("Please write something!");
            return;
        }
        // show progressfialog
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.loadingText));
        progressDialog.setCancelable(false);
        progressDialog.show();

        final String url = getResources().getString(R.string.baseUrl) + "comment/create?commentBody="
                + this.commentBoxEditText.getText().toString()
                + "&listId=" + this.listId
                + "&accountId=" + AccountKit.getCurrentAccessToken().getAccountId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(getActivity()).fetchPostResponse(url);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog.isShowing()) progressDialog.cancel();
                                if (response.code() == ResourceProvider.RESPONSE_CODE_CREATED) {
                                    fetchComments(listId);
                                    commentBoxEditText.setText("");
                                } else
                                    Commons.showSimpleToast(getActivity().getApplicationContext(), "Can not post comment!");
                            }
                        });
                    }
                } catch (IOException e) {
                    if (progressDialog.isShowing()) progressDialog.cancel();
                    Log.e("POST_COMMENT", e.toString());
                }
            }
        }).start();
    }
}
