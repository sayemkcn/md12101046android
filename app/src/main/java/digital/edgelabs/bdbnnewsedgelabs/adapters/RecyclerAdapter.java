package digital.edgelabs.bdbnnewsedgelabs.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import digital.edgelabs.bdbnnewsedgelabs.BookmarkActivity;
import digital.edgelabs.bdbnnewsedgelabs.OfflineNewsActivity;
import digital.edgelabs.bdbnnewsedgelabs.commons.Pref;
import digital.edgelabs.bdbnnewsedgelabs.DetailsActivity;
import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.entity.Movie;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Movie> movieList;
    private Activity context;

    public RecyclerAdapter(Activity context, List<Movie> movieList) {
        this.inflater = LayoutInflater.from(context);
        this.movieList = movieList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.single_recycler_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Movie movie = this.movieList.get(position);
        Glide.with(context).load(movie.getImageUrl()).centerCrop().placeholder(R.mipmap.ic_launcher).diskCacheStrategy(DiskCacheStrategy.ALL).into(myViewHolder.imageView);
        myViewHolder.titleTextView.setText(movie.getName());
        myViewHolder.directorTextView.setText(movie.getDirectorName());
        myViewHolder.castsTextView.setText(movie.getCast());
    }

    @Override
    public int getItemCount() {
        return this.movieList.size();
    }

    public void remove(int position) {
        if (position < 0 || position >= movieList.size()) {
            return;
        }
        movieList.remove(position);
        notifyItemRemoved(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView directorTextView;
        TextView castsTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            directorTextView = (TextView) itemView.findViewById(R.id.directorTextView);
            castsTextView = (TextView) itemView.findViewById(R.id.castsTextView);

            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
            titleTextView.setTypeface(typeface);
            directorTextView.setTypeface(typeface);
            castsTextView.setTypeface(typeface);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(context instanceof OfflineNewsActivity))
                        context.startActivity(new Intent(context, DetailsActivity.class).putExtra("newsId", movieList.get(getAdapterPosition()).getId())
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    else if (context instanceof OfflineNewsActivity) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("offlineNewsItem", movieList.get(getAdapterPosition()));
//                        Log.d("NEWS",newsList.get(getAdapterPosition()).toString());
                        context.startActivity(new Intent(context, DetailsActivity.class).putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
            });

            // to delete item
            if (context instanceof BookmarkActivity || context instanceof OfflineNewsActivity) {
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
                                        if (context instanceof BookmarkActivity)
                                            removeItem(getAdapterPosition(), Pref.PREF_KEY_BOOKMARK_LIST);
                                        else if (context instanceof OfflineNewsActivity)
                                            removeItem(getAdapterPosition(), Pref.PREF_KEY_OFFLINE_NEWS_LIST);

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

    private void removeItem(int position, String preferenceKey) {
        Gson gson = new Gson();
        String newsListJson = Pref.getPreferenceString(context, preferenceKey);
        List<NewsEntity> newsList;
        if (newsListJson != null && !newsListJson.equals("")) {
            newsList = gson.fromJson(newsListJson, new TypeToken<List<NewsEntity>>() {
            }.getType());
            newsList.remove(position);
            newsListJson = gson.toJson(newsList);
            Pref.savePreference(context, preferenceKey, newsListJson);
        }
        this.remove(position);
    }
}