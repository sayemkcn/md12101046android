package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.accountkit.AccountKit;

import net.toracode.moviedb.ListItemsActivity;
import net.toracode.moviedb.PreferenceActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.CustomList;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;

public class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<CustomList> listOfCustomList;
    private Activity context;

    private static final String FOLLOW_BUTTON_TEXT = "Follow";
    private static final String UNFOLLOW_BUTTON_TEXT = "Unfollow";

    public CustomListAdapter(Activity context, List<CustomList> listOfCustomList) {
        this.inflater = LayoutInflater.from(context);
        this.listOfCustomList = listOfCustomList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.single_customlist_recycler_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        CustomList list = this.listOfCustomList.get(position);
        myViewHolder.titleTextView.setText((position + 1) + ". " + list.getTitle());
        myViewHolder.typeTextView.setText(list.getType());
        myViewHolder.descriptionTextView.setText(list.getDescription());

        // initialize like/ follow button state
        if (AccountKit.getCurrentAccessToken() != null) {
            String accountId = AccountKit.getCurrentAccessToken().getAccountId();
            if (list.getUser().getAccountId().equals(accountId)) {
                myViewHolder.followButton.setText("Edit");
                myViewHolder.followButton.setTextColor(context.getResources().getColor(android.R.color.holo_blue_bright));
//                myViewHolder.followButton.setEnabled(false);
            } else {
                checkFollowing(myViewHolder.followButton, list.getUniqueId(), AccountKit.getCurrentAccessToken().getAccountId());
            }
        }

    }

    @Override
    public int getItemCount() {
        return this.listOfCustomList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView typeTextView;
        TextView descriptionTextView;
        Button likeButton;
        Button commentButton;
        Button followButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            typeTextView = (TextView) itemView.findViewById(R.id.typeTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            likeButton = (Button) itemView.findViewById(R.id.likeButton);
            commentButton = (Button) itemView.findViewById(R.id.commentButton);
            followButton = (Button) itemView.findViewById(R.id.followButton);

            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
            titleTextView.setTypeface(typeface);
            typeTextView.setTypeface(typeface);
            descriptionTextView.setTypeface(typeface);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Log.i("PARENT_ADAPTER",listOfCustomList.get(getAdapterPosition()).getUniqueId()+"");
                    context.startActivity(
                            new Intent(context, ListItemsActivity.class)
                                    .putExtra("listId", listOfCustomList.get(getAdapterPosition()).getUniqueId())
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    );

                    Log.d("ADAPTER_POS", getAdapterPosition() + "");
                }
            });

            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AccountKit.getCurrentAccessToken() == null) {
                        context.startActivity(new Intent(context, PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        return;
                    }
                    if (((Button) view).getText().toString().toLowerCase().equals(FOLLOW_BUTTON_TEXT.toLowerCase()))
                        followList((Button) view, listOfCustomList.get(getAdapterPosition()).getUniqueId(), AccountKit.getCurrentAccessToken().getAccountId());
                    else if (((Button) view).getText().toString().toLowerCase().equals(UNFOLLOW_BUTTON_TEXT.toLowerCase()))
                        unFollowList((Button) view, listOfCustomList.get(getAdapterPosition()).getUniqueId(), AccountKit.getCurrentAccessToken().getAccountId());
                    else
                        editCustomList((Button) view, listOfCustomList.get(getAdapterPosition()), AccountKit.getCurrentAccessToken().getAccountId());
                }
            });


            // ITEM ON LONG CLICK
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(context)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle("Delete List")
                            .setMessage("Are you sure you want to remove this list?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // delete the list
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return false;
                }
            });

        }
    }


    // -----------FOLLOW BUTTON------------ //

    // call server with list id and account id
    // unfollows a list
    private void unFollowList(final Button button, Long uniqueId, String accountId) {
        final String url = context.getResources().getString(R.string.baseUrl) + "list/unfollow/" + uniqueId + "?accountId=" + accountId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(context).fetchPostResponse(url);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_ACCEPTED) {
                                button.setText(FOLLOW_BUTTON_TEXT);
                                button.setTextColor(context.getResources().getColor(android.R.color.black));
                            } else {
                                Toast.makeText(context, "Can not unfollow list", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e("UNFOLLOW_LIST", e.toString());
                }
            }
        }).start();
    }

    // call server with list id and account id
    // Follows a list
    private void followList(final Button button, Long uniqueId, String accountId) {
        final String url = context.getResources().getString(R.string.baseUrl) + "list/follow/" + uniqueId + "?accountId=" + accountId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(context).fetchPostResponse(url);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_ACCEPTED) {
                                button.setText(UNFOLLOW_BUTTON_TEXT);
                                button.setTextColor(context.getResources().getColor(android.R.color.holo_blue_bright));
                            } else {
                                Toast.makeText(context, "Can not follow list", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e("FOLLOW_LIST", e.toString());
                }
            }
        }).start();
    }

    private void checkFollowing(final Button followButton, Long uniqueId, String accountId) {
        final String url = context.getResources().getString(R.string.baseUrl) + "list/isFollowing/" + uniqueId + "?accountId=" + accountId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(context).fetchPostResponse(url);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_FOUND) {
                                followButton.setText(UNFOLLOW_BUTTON_TEXT);
                                followButton.setTextColor(context.getResources().getColor(android.R.color.holo_blue_bright));
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e("CHECK_FOLLOW_LIST", e.toString());
                }
            }
        }).start();
    }

    // ------END FOLLOW BUTTON------ //

    // -------EDIT BUTTON------ //
    private void editCustomList(Button view, final CustomList list, final String accountId) {
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
                        EditText typeEditText = (EditText) view.findViewById(R.id.listType);
                        String name = nameEditText.getText().toString();
                        String desc = descEditText.getText().toString();
                        String type = typeEditText.getText().toString();
//
                        submitCustomList(list, accountId, name, desc, type);
                    }
                })
                .negativeText("Cancel")
                .canceledOnTouchOutside(false)
                .build();
        EditText nameEditText = (EditText) dialog.getCustomView().findViewById(R.id.listName);
        EditText descEditText = (EditText) dialog.getCustomView().findViewById(R.id.listDescription);
        EditText typeEditText = (EditText) dialog.getCustomView().findViewById(R.id.listType);
        nameEditText.setText(list.getTitle());
        descEditText.setText(list.getDescription());
        typeEditText.setText(list.getType());
        dialog.show();
    }

    // post edited list to server
    private void submitCustomList(CustomList list, String accountId,final String name, String desc, String type) {
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
    // -------END EDIT BUTTON ACTION------- //
}