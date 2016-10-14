package digital.edgelabs.bdbnnewsedgelabs.adapters;

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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import digital.edgelabs.bdbnnewsedgelabs.DetailsActivity;
import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;

public class FeaturedRecyclerAdapter extends RecyclerView.Adapter<FeaturedRecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<NewsEntity> newsList;
    private Activity context;

    public FeaturedRecyclerAdapter(Activity context, List<NewsEntity> newsList) {
        this.inflater = LayoutInflater.from(context);
        this.newsList = newsList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.single_news_item_featured, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        NewsEntity news = this.newsList.get(position);
        Glide.with(context).load(news.getImageUrl()).centerCrop().placeholder(R.mipmap.ic_launcher).diskCacheStrategy(DiskCacheStrategy.ALL).into(myViewHolder.newsImageView);
        myViewHolder.titleTextView.setText(news.getTitle().replace("\n", ""));

        String newsSummary = news.getDetails();
        if (newsSummary.length() > 40)
            myViewHolder.summaryTextView.setText(newsSummary.substring(0, 40).replace("\n", "") + "..");
        else
            myViewHolder.summaryTextView.setText(newsSummary.replace("\n", "") + "..");

        Glide.with(context).load(news.getNewsSourceEntity().getIconUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.mipmap.ic_launcher).crossFade().into(myViewHolder.sourceLogoImageView);
        myViewHolder.sourceNameTextView.setText(news.getNewsSourceEntity().getName());
        myViewHolder.newsTimeTextView.setText(Commons.computeTimeDiff(news.getLastUpdated(), new Date()).get(TimeUnit.HOURS).toString() + " " + context.getResources().getString(R.string.hourBefore));
    }

    @Override
    public int getItemCount() {
        return this.newsList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImageView;
        TextView titleTextView;
        TextView summaryTextView;
        ImageView sourceLogoImageView;
        TextView sourceNameTextView;
        TextView newsTimeTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            newsImageView = (ImageView) itemView.findViewById(R.id.newsImageView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            summaryTextView = (TextView) itemView.findViewById(R.id.newsSummaryTextView);
            sourceLogoImageView = (ImageView) itemView.findViewById(R.id.sourceLogoImageView);
            sourceNameTextView = (TextView) itemView.findViewById(R.id.sourceNameTextView);
            newsTimeTextView = (TextView) itemView.findViewById(R.id.timeTextView);

            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
            titleTextView.setTypeface(typeface);
            summaryTextView.setTypeface(typeface);
            sourceNameTextView.setTypeface(typeface);
            newsTimeTextView.setTypeface(typeface);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, DetailsActivity.class).putExtra("newsId", newsList.get(getAdapterPosition()).getId())
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });

        }
    }

}