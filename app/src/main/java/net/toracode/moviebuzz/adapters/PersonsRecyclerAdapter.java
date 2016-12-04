package net.toracode.moviebuzz.adapters;

import android.app.Activity;
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

import net.toracode.moviebuzz.R;
import net.toracode.moviebuzz.entity.Person;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Random;

public class PersonsRecyclerAdapter extends RecyclerView.Adapter<PersonsRecyclerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Person> personList;
    private Activity context;
    private int lastPosition = -1;

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

        this.setAnimation(myViewHolder.itemView,position);
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

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(new Random().nextInt(2001));//to make duration random number between [0,501)
        viewToAnimate.startAnimation(anim);
        lastPosition = position;
    }
}