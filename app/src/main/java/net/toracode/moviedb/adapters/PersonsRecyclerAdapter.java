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

import net.toracode.moviedb.DetailsActivity;
import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.Person;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class PersonsRecyclerAdapter extends RecyclerView.Adapter<PersonsRecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Person> personList;
    private Activity context;

    public PersonsRecyclerAdapter(Activity context, List<Person> personList) {
        this.inflater = LayoutInflater.from(context);
        this.personList = personList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.single_cast_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        Person person = this.personList.get(position);
        String imageUrl = context.getResources().getString(R.string.baseUrl) + "person/image/" + person.getUniqueId();
        Glide.with(context).load(imageUrl).centerCrop().placeholder(R.mipmap.ic_launcher).diskCacheStrategy(DiskCacheStrategy.ALL).into(myViewHolder.imageView);
        myViewHolder.nameTextView.setText(person.getName());
        myViewHolder.designationsTextView.setText(StringUtils.join(person.getDesignations(), ","));
    }

    @Override
    public int getItemCount() {
        return this.personList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView designationsTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            designationsTextView = (TextView) itemView.findViewById(R.id.designationsTextView);

//            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
//            nameTextView.setTypeface(typeface);
//            designationsTextView.setTypeface(typeface);


        }
    }

}