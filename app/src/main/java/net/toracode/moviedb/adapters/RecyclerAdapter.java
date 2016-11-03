package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
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

import net.toracode.moviedb.DetailsActivity;
import net.toracode.moviedb.OfflineActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.commons.Pref;
import net.toracode.moviedb.entity.Movie;
import net.toracode.moviedb.entity.Person;

import java.text.SimpleDateFormat;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Movie> movieList;
    private Activity context;
    private String prefKey = null;

    public RecyclerAdapter(Activity context, List<Movie> movieList) {
        this.inflater = LayoutInflater.from(context);
        this.movieList = movieList;
        this.context = context;
    }

    public void setPrefKey(String prefKey) {
        this.prefKey = prefKey;
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        if (movie.getReleaseDate() != null)
            myViewHolder.releaseDateTextView.setText(context.getString(R.string.releaseDateTextBangla) + " " + sdf.format(movie.getReleaseDate()));
        else
            myViewHolder.releaseDateTextView.setText(context.getResources().getString(R.string.waitingText));

        myViewHolder.industryTextView.setText(movie.getIndustry());
        myViewHolder.genereTextView.setText(movie.getGenere());
        myViewHolder.castsTextView.setText(context.getResources().getString(R.string.castTextBangla) + "\n" + this.getCommaSeperatedCastsString(movie.getCastAndCrewList()));
        myViewHolder.filmRatingTextView.setText(context.getString(R.string.filmRatingText) + " " + movie.getRated());
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
        TextView releaseDateTextView;
        TextView industryTextView;
        TextView genereTextView;
        TextView castsTextView;
        TextView filmRatingTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            industryTextView = (TextView) itemView.findViewById(R.id.industryTextView);
            castsTextView = (TextView) itemView.findViewById(R.id.castsTextView);
            releaseDateTextView = (TextView) itemView.findViewById(R.id.releaseDateTextView);
            filmRatingTextView = (TextView) itemView.findViewById(R.id.filmRatingTextView);
            genereTextView = (TextView) itemView.findViewById(R.id.genereTextView);

            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
            titleTextView.setTypeface(typeface);
            industryTextView.setTypeface(typeface);
            castsTextView.setTypeface(typeface);
            releaseDateTextView.setTypeface(typeface);
            filmRatingTextView.setTypeface(typeface);
            genereTextView.setTypeface(typeface);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, DetailsActivity.class).putExtra("movie", movieList.get(getAdapterPosition()))
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });

            // to delete item
            if (context instanceof OfflineActivity) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        new AlertDialog.Builder(context)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle("Delete Item")
                                .setMessage("Are you sure you want to remove this item from your saved list?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (prefKey != null)
                                            removeItem(getAdapterPosition(), prefKey);
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
        String movieListJson = Pref.getPreferenceString(context, preferenceKey);
        List<Movie> movieList;
        if (movieListJson != null && !movieListJson.equals("")) {
            movieList = gson.fromJson(movieListJson, new TypeToken<List<Movie>>() {
            }.getType());
            movieList.remove(position);
            movieListJson = gson.toJson(movieList);
            Pref.savePreference(context, preferenceKey, movieListJson);
        }
        this.remove(position);
    }

    private String getCommaSeperatedCastsString(List<Person> personList) {
        StringBuilder casts = new StringBuilder();
        for (int i = 0; personList != null && i < personList.size(); i++) {
            Person cast = personList.get(i);
            casts.append(cast.getName());
            int indexNumber = i + 1;
            if (indexNumber != personList.size()) {
                casts.append(",");
            }
        }
        return casts.toString();
    }
}