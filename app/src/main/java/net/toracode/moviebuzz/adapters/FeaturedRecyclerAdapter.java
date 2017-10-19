package net.toracode.moviebuzz.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;
import java.util.Random;

import net.toracode.moviebuzz.DetailsActivity;
import net.toracode.moviebuzz.R;
import net.toracode.moviebuzz.entity.Movie;

public class FeaturedRecyclerAdapter extends RecyclerView.Adapter<FeaturedRecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Movie> movieList;
    private Activity context;
    private int lastPosition = -1;

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
        String imageUrl = context.getResources().getString(R.string.baseUrl)+"movie/image/"+movie.getUniqueId();
        Glide.with(context).load(imageUrl).centerCrop().placeholder(R.mipmap.ic_launcher).diskCacheStrategy(DiskCacheStrategy.ALL).into(myViewHolder.imageView);
        myViewHolder.titleTextView.setText(movie.getName());
        myViewHolder.directorTextView.setText(movie.getProductionHouse());

        this.setAnimation(myViewHolder.itemView,position);
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
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(new Random().nextInt(2001));//to make duration random number between [0,501)
        viewToAnimate.startAnimation(anim);
        lastPosition = position;
    }
}