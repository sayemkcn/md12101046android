package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.toracode.moviedb.ListItemsActivity;
import net.toracode.moviedb.OfflineActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.CustomList;

import java.util.List;

public class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<CustomList> listOfCustomList;
    private Activity context;

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
    }

    @Override
    public int getItemCount() {
        return this.listOfCustomList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView typeTextView;
        TextView descriptionTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            typeTextView = (TextView) itemView.findViewById(R.id.typeTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);

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
                                    .putExtra("listId",listOfCustomList.get(getAdapterPosition()).getUniqueId())
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    );

                }
            });

            // to delete item
            if (context instanceof OfflineActivity) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        new AlertDialog.Builder(context)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle("Delete Bookmark")
                                .setMessage("Are you sure you want to remove this item from your bookmark list?")
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
    }


}