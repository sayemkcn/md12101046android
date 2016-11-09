package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.SuperToast;

import net.toracode.moviedb.PreferenceActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.Review;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import okhttp3.Response;

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

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                            .title("Edit review")
                            .customView(R.layout.review_box_layout, false);
                    MaterialDialog dialog = builder.show();
                    View v = dialog.getCustomView();

                    EditText titleEditText = (EditText) v.findViewById(R.id.reviewTitle);
                    EditText messageEditText = (EditText) v.findViewById(R.id.reviewMessage);
                    RatingBar ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);
                    Button postReviewButton = (Button) v.findViewById(R.id.postReviewButton);

                    Review review = reviewList.get(getAdapterPosition());
                    titleEditText.setText(review.getTitle());
                    messageEditText.setText(review.getMessage());
                    ratingBar.setRating(review.getRating());

                    submitEditedReview(dialog, getAdapterPosition(), postReviewButton, titleEditText, messageEditText, ratingBar, review);
                }
            });

        }
    }

    private void submitEditedReview(final MaterialDialog dialog,
                                    final int position,
                                    Button postReviewButton,
                                    final EditText titleEditText,
                                    final EditText messageEditText,
                                    final RatingBar ratingBar,
                                    final Review review) {
        postReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SuperToast loadingToast = Commons.getLoadingToast(context);
                loadingToast.show();
                review.setTitle(titleEditText.getText().toString());
                review.setMessage(messageEditText.getText().toString());
                review.setRating(ratingBar.getRating());
                final String url = context.getResources().getString(R.string.baseUrl) + "review/update/" + review.getUniqueId() + "?title=" + review.getTitle() + "&message=" + review.getMessage() + "&rating=" + review.getRating()
                        + "&accountId=" + accountId + "&movieId=" + review.getMovie().getUniqueId();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Response response = new ResourceProvider(context).fetchPostResponse(url);
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (loadingToast.isShowing()) loadingToast.dismiss();
                                    dialog.dismiss();
                                    if (response.code() == ResourceProvider.RESPONSE_CODE_FORBIDDEN)
                                        context.startActivity(new Intent(context, PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                    else if (response.code() == ResourceProvider.RESPONSE_NOT_ACCEPTABLE)
                                        Commons.showSimpleToast(context, "Can not edit review.");
                                    else if (response.code() == ResourceProvider.RESPONSE_CODE_CREATED) {
                                        Commons.showDialog(context,"Successful!","You\'ve edited your review.");
                                        notifyItemChanged(position);
                                    }
                                }
                            });

                        } catch (IOException e) {
                            Log.d("EDIT_REVIEW", e.toString());
                        }
                    }
                }).start();
            }
        });
    }


}