package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.Review;

import java.text.SimpleDateFormat;
import java.util.List;

public class ReviewRecyclerAdapter extends RecyclerView.Adapter<ReviewRecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Review> reviewList;
    private Activity context;
    private String accountId = null;

    public ReviewRecyclerAdapter(Activity context, List<Review> reviewList) {
        this.inflater = LayoutInflater.from(context);
        this.reviewList = reviewList;
        this.context = context;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        if (review.getLastUpdated() != null)
            myViewHolder.dateTextView.setText(sdf.format(review.getLastUpdated()));
        else if (review.getCreated() != null)
            myViewHolder.dateTextView.setText(sdf.format(review.getCreated()));

        // hide edit button for other users review
        if (!review.getUser().getAccountId().equals(accountId)) {
            myViewHolder.editButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.reviewList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView usersNameTextView;
        RatingBar ratingBar;
        TextView titleTextView;
        TextView messageTextView;
        TextView dateTextView;
        ImageButton editButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            usersNameTextView = (TextView) itemView.findViewById(R.id.userNameTextView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            editButton = (ImageButton) itemView.findViewById(R.id.editButton);

            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
            usersNameTextView.setTypeface(typeface);
            titleTextView.setTypeface(typeface);
            messageTextView.setTypeface(typeface);

        }
    }


}