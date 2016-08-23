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

import java.util.Date;
import java.util.concurrent.TimeUnit;

import digital.edgelabs.bdbnnewsedgelabs.DetailsActivity;
import digital.edgelabs.bdbnnewsedgelabs.R;
import digital.edgelabs.bdbnnewsedgelabs.entity.CategoryEntity;
import digital.edgelabs.bdbnnewsedgelabs.entity.NewsEntity;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private CategoryEntity categoryEntity;
    private Activity context;

    public RecyclerAdapter(Activity context, CategoryEntity categoryEntity) {
        this.inflater = LayoutInflater.from(context);
        this.categoryEntity = categoryEntity;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.single_news_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        NewsEntity news = categoryEntity.getNewsEntityList().get(position);
        Glide.with(context).load(news.getImageUrl()).into(myViewHolder.newsImageView);
        myViewHolder.titleTextView.setText(news.getTitle());
        myViewHolder.summaryTextView.setText(news.getDetails());
        Glide.with(context).load(news.getNewsSourceEntity().getIconUrl()).placeholder(R.mipmap.ic_launcher).crossFade().into(myViewHolder.sourceLogoImageView);
        myViewHolder.sourceNameTextView.setText(news.getNewsSourceEntity().getName());
        myViewHolder.newsTimeTextView.setText(Commons.computeTimeDiff(news.getLastUpdated(), new Date()).get(TimeUnit.HOURS).toString() + " " + context.getResources().getString(R.string.hourBefore));
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(news.getLastUpdated());
//        myViewHolder.newsTimeTextView.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + ", " + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + " " + calendar.get(Calendar.DAY_OF_MONTH));

    }

    @Override
    public int getItemCount() {
        return categoryEntity.getNewsEntityList().size();
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, DetailsActivity.class).putExtra("newsId", categoryEntity.getNewsEntityList().get(getAdapterPosition()).getId())
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });
        }
    }
}