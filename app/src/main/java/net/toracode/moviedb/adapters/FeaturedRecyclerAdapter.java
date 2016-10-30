package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import net.toracode.moviedb.DetailsActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.Movie;

public class FeaturedRecyclerAdapter extends RecyclerView.Adapter<FeaturedRecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Movie> movieList;
    private Activity context;

    public FeaturedRecyclerAdapter(Activity context, List<Movie> movieList) {
        this.inflater = LayoutInflater.from(context);
        this.movieList = movieList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.single_movie_item_featured, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Movie movie = this.movieList.get(position);
        Glide.with(context).load(movie.getImageUrl()).centerCrop().placeholder(R.mipmap.ic_launcher).diskCacheStrategy(DiskCacheStrategy.ALL).into(myViewHolder.imageView);
        myViewHolder.titleTextView.setText(movie.getName());
        myViewHolder.directorTextView.setText(movie.getProductionHouse());
    }

    @Override
    public int getItemCount() {
        return this.movieList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView directorTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.movieImageView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            directorTextView = (TextView) itemView.findViewById(R.id.directorNameTextView);

            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
            titleTextView.setTypeface(typeface);
            directorTextView.setTypeface(typeface);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, DetailsActivity.class).putExtra("movie", movieList.get(getAdapterPosition()))
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });

        }
    }

}