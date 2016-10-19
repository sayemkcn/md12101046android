package net.toracode.moviedb.adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import net.toracode.moviedb.R;
import net.toracode.moviedb.entity.PreferenceSingleItem;
import net.toracode.moviedb.events.PrefChangeEvent;

public class PreferenceAdapter extends RecyclerView.Adapter<PreferenceAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<PreferenceSingleItem> preferenceList;
    private Activity context;

    public PreferenceAdapter(Activity context, List<PreferenceSingleItem> preferenceList) {
        this.inflater = LayoutInflater.from(context);
        this.preferenceList = preferenceList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.single_pref_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
        PreferenceSingleItem prefItem = this.preferenceList.get(position);

        myViewHolder.nameTextView.setText(prefItem.getName());
        myViewHolder.valueCheckBox.setChecked(prefItem.isActivated());

    }

    @Override
    public int getItemCount() {
        return this.preferenceList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        CheckBox valueCheckBox;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.nameTextView = (TextView) itemView.findViewById(R.id.newsSourceName);
            this.valueCheckBox = (CheckBox) itemView.findViewById(R.id.newsSourceCheckBox);

            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SolaimanLipi.ttf");
            this.nameTextView.setTypeface(typeface);
            this.valueCheckBox.setTypeface(typeface);

            this.valueCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    EventBus.getDefault().post(new PrefChangeEvent(compoundButton, getAdapterPosition() + 1, checked));
                }
            });
        }
    }
}