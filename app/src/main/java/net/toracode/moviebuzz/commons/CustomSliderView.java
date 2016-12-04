package net.toracode.moviebuzz.commons;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;

import net.toracode.moviebuzz.R;

/**
 * Created by sayemkcn on 10/1/16.
 */
public class CustomSliderView extends BaseSliderView {
    private String iconUrl = null;
    private Context context;

    public CustomSliderView(Context context, String iconUrl) {
        super(context);
        this.context = context;
        this.iconUrl = iconUrl;
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.slider_custom_layout, null);
        ImageView target = (ImageView) v.findViewById(R.id.daimajia_slider_image);
        TextView description = (TextView) v.findViewById(R.id.description);
        ImageView iconView = (ImageView) v.findViewById(R.id.sliderNewsSourceIcon);
        Glide.with(context).load(iconUrl).placeholder(R.mipmap.ic_launcher).into(iconView);
        description.setText(getDescription());
        bindEventAndShow(v, target);
        return v;
    }
}
