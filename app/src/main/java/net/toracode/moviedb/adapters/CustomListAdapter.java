package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccountKit;

import net.toracode.moviedb.ListFragmentsActivity;
import net.toracode.moviedb.ListItemsActivity;
import net.toracode.moviedb.PreferenceActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.commons.CustomListOperations;
import net.toracode.moviedb.entity.CustomList;
import net.toracode.moviedb.service.ResourceProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
        myViewHolder.titleTextView.setText(list.getTitle());
        myViewHolder.typeTextView.setText(list.getType());
        myViewHolder.descriptionTextView.setText(list.getDescription());
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a 'on' MMM dd, yyyy");
        if (list.getLastUpdated() != null)
            myViewHolder.dateTimeTextView.setText("Last updated on " + sdf.format(list.getLastUpdated()));
        else
            myViewHolder.dateTimeTextView.setText("Created at " + sdf.format(list.getCreated()));
        // initialize like/ follow button state
        if (AccountKit.getCurrentAccessToken() != null) {
            String accountId = AccountKit.getCurrentAccessToken().getAccountId();
            if (list.getUser().getAccountId().equals(accountId)) {
                myViewHolder.followButton.setText("Edit");
                myViewHolder.followButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_create_black_18dp, 0, 0, 0);
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
        TextView dateTimeTextView;
        Button commentButton;
        Button followButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            typeTextView = (TextView) itemView.findViewById(R.id.typeTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            dateTimeTextView = (TextView) itemView.findViewById(R.id.dateTimeTextView);
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
                        new CustomListOperations(context).showEditListDialog((Button) view, listOfCustomList.get(getAdapterPosition()), AccountKit.getCurrentAccessToken().getAccountId());
                }
            });

            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    commentButtonClicked(listOfCustomList.get(getAdapterPosition()).getUniqueId());
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

    private void commentButtonClicked(Long listId) {
        Bundle bundle = new Bundle();
        bundle.putString("ref", "CustomListAdapterCommentButton");
        bundle.putLong("listId", listId);
        Intent intent = new Intent(context, ListFragmentsActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
                                button.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_favorite_border_black_18dp, 0, 0, 0);
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
                                button.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_favorite_black_18dp, 0, 0, 0);
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
                                followButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_favorite_black_18dp, 0, 0, 0);
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

}