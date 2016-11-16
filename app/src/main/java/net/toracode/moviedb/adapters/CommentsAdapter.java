package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.accountkit.AccountKit;

import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.Comment;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import okhttp3.Response;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Comment> commentList;
    private Activity context;

    public CommentsAdapter(Activity context, List<Comment> commentList) {
        this.inflater = LayoutInflater.from(context);
        this.commentList = commentList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.comment_single_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Comment comment = this.commentList.get(position);
        myViewHolder.nameTextView.setText(comment.getUser().getName());
        myViewHolder.commentBodyTextView.setText(comment.getCommentBody());
        SimpleDateFormat sdf = new SimpleDateFormat(" hh:mm:ss a 'on' MMM dd, yyyy");
        if (comment.getLastUpdated() != null)
            myViewHolder.dateTimeTextView.setText(sdf.format(comment.getLastUpdated()));
        else
            myViewHolder.dateTimeTextView.setText(sdf.format(comment.getCreated()));

        if (AccountKit.getCurrentAccessToken() != null) {
            if (comment.getUser().getAccountId().equals(AccountKit.getCurrentAccessToken().getAccountId())) {
                myViewHolder.actionLayout.setVisibility(View.VISIBLE);
            } else
                myViewHolder.actionLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.commentList.size();
    }

    public void remove(int position) {
        if (position < 0 || position >= commentList.size()) {
            return;
        }
        commentList.remove(position);
        notifyItemRemoved(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView commentBodyTextView;
        TextView dateTimeTextView;
        View actionLayout;
        ImageButton editButton;
        ImageButton deleteButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            commentBodyTextView = (TextView) itemView.findViewById(R.id.commentBodyTextView);
            dateTimeTextView = (TextView) itemView.findViewById(R.id.dateTimeTextView);
            actionLayout = itemView.findViewById(R.id.actionLayout);
            editButton = (ImageButton) itemView.findViewById(R.id.editButton);
            deleteButton = (ImageButton) itemView.findViewById(R.id.deleteButton);

//            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
//            nameTextView.setTypeface(typeface);
//            commentBodyTextView.setTypeface(typeface);
//            dateTimeTextView.setTypeface(typeface);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AccountKit.getCurrentAccessToken() != null)
                        editButtonClicked(commentList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AccountKit.getCurrentAccessToken() != null)
                        deleteButtonClicked(commentList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

        }
    }

    // -----DELETE BUTTON ACTION-------- //
    private void deleteButtonClicked(final Comment comment, final int adapterPosition) {
        new MaterialDialog.Builder(context)
                .title("DELETE")
                .content("Are you sure?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deleteComment(comment, adapterPosition);
                    }
                })
                .show();
    }

    private void deleteComment(Comment comment, final int adapterPosition) {
        final String url = context.getResources().getString(R.string.baseUrl) + "comment/delete/" + comment.getUniqueId() + "?accountId=" + AccountKit.getCurrentAccessToken().getAccountId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(context).fetchPostResponse(url);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_OK) {
                                remove(adapterPosition);
                                Commons.showSimpleToast(context, "Comment deleted!");
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_FORBIDDEN)
                                Commons.showSimpleToast(context, "You can\'t delete this comment. It\'s not your comment.");
                            else
                                Commons.showSimpleToast(context, "Can not delete comment!");
                        }
                    });

                } catch (IOException e) {
                    Log.e("DELETE_COMMENT", e.toString());
                }
            }
        }).start();
    }
    // -----END DELETE BUTTON ACTION-------- //

    // -------EDIT BUTTON ACTION------ //
    private void editButtonClicked(final Comment comment, final int adapterPosition) {
        new MaterialDialog.Builder(context)
                .title("Edit Comment")
                .inputRange(1, 255)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("", comment.getCommentBody(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        comment.setCommentBody(input.toString());
                        submitEditedComment(comment, adapterPosition);
                    }
                })
                .positiveText("Submit")
                .negativeText("Cancel")
                .show();
    }

    private void submitEditedComment(Comment comment, final int adapterPosition) {
        final String url = context.getResources().getString(R.string.baseUrl) + "comment/edit/" + comment.getUniqueId() + "?commentBody=" + comment.getCommentBody() + "&accountId=" + AccountKit.getCurrentAccessToken().getAccountId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(context).fetchPostResponse(url);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_ACCEPTED) {
                                notifyItemChanged(adapterPosition);
                                Commons.showSimpleToast(context, "Successful!");
                            } else
                                Commons.showSimpleToast(context, "Can not edit comment!");
                        }
                    });

                } catch (IOException e) {
                    Log.e("DELETE_COMMENT", e.toString());
                }
            }
        }).start();
    }


}