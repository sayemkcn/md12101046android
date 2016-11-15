package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.toracode.moviedb.R;
import net.toracode.moviedb.commons.Pref;
import net.toracode.moviedb.entity.Comment;
import net.toracode.moviedb.entity.Movie;

import java.text.SimpleDateFormat;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Comment> commentList;
    private Activity context;

    public CommentsAdapter(Activity context, List<Comment> commentList) {
        this.inflater = LayoutInflater.from(context);
        this.commentList = commentList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.comment_single_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Comment comment = this.commentList.get(position);
        myViewHolder.nameTextView.setText(comment.getUser().getName());
        myViewHolder.commentBodyTextView.setText(comment.getCommentBody());
        SimpleDateFormat sdf = new SimpleDateFormat(" hh:mm:ss a 'on' MMM dd, yyyy");
        if (comment.getLastUpdated() != null)
            myViewHolder.dateTimeTextView.setText(sdf.format(comment.getLastUpdated()));
        else
            myViewHolder.dateTimeTextView.setText(sdf.format(comment.getCreated()));
    }

    @Override
    public int getItemCount() {
        return this.commentList.size();
    }

    public void remove(int position) {
        if (position < 0 || position >= commentList.size()) {
            return;
        }
        commentList.remove(position);
        notifyItemRemoved(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView commentBodyTextView;
        TextView dateTimeTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            commentBodyTextView = (TextView) itemView.findViewById(R.id.commentBodyTextView);
            dateTimeTextView = (TextView) itemView.findViewById(R.id.dateTimeTextView);

//            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
//            nameTextView.setTypeface(typeface);
//            commentBodyTextView.setTypeface(typeface);
//            dateTimeTextView.setTypeface(typeface);

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

}