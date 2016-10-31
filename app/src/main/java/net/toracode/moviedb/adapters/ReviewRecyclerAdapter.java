package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.Person;
import net.toracode.moviedb.entity.Review;

import java.util.List;

public class ReviewRecyclerAdapter extends RecyclerView.Adapter<ReviewRecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Review> reviewList;
    private Activity context;
    private String prefKey = null;

    public ReviewRecyclerAdapter(Activity context, List<Review> reviewList) {
        this.inflater = LayoutInflater.from(context);
        this.reviewList = reviewList;
        this.context = context;
    }

    public void setPrefKey(String prefKey) {
        this.prefKey = prefKey;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.single_review_recycler_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Review review = this.reviewList.get(position);
        myViewHolder.usersNameTextView.setText(review.getUser().getName());
        myViewHolder.ratingBar.setRating(review.getRating());
        myViewHolder.titleTextView.setText(review.getTitle());
        myViewHolder.messageTextView.setText(review.getMessage());
    }

    @Override
    public int getItemCount() {
        return this.reviewList.size();
    }

    public void remove(int position) {
        if (position < 0 || position >= reviewList.size()) {
            return;
        }
        reviewList.remove(position);
        notifyItemRemoved(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView usersNameTextView;
        RatingBar ratingBar;
        TextView titleTextView;
        TextView messageTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            usersNameTextView = (TextView) itemView.findViewById(R.id.userNameTextView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);


            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
            usersNameTextView.setTypeface(typeface);
            titleTextView.setTypeface(typeface);
            messageTextView.setTypeface(typeface);

        }
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