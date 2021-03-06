package net.toracode.moviebuzz.commons;


import android.view.View;

import com.daimajia.androidanimations.library.bouncing_entrances.BounceInAnimator;
import com.daimajia.slider.library.Animations.BaseAnimationInterface;

/**
 * Created by sayemkcn on 10/1/16.
 */
public class SliderChildAnimator implements BaseAnimationInterface {
//    private final static String TAG = "ChildAnimation";

    @Override
    public void onPrepareCurrentItemLeaveScreen(View current) {
        View descriptionLayout = current.findViewById(com.daimajia.slider.library.R.id.description_layout);
        if (descriptionLayout != null) {
            current.findViewById(com.daimajia.slider.library.R.id.description_layout).setVisibility(View.INVISIBLE);
        }
//        Log.e(TAG, "onPrepareCurrentItemLeaveScreen called");
    }

    @Override
    public void onPrepareNextItemShowInScreen(View next) {
        View descriptionLayout = next.findViewById(com.daimajia.slider.library.R.id.description_layout);
        if (descriptionLayout != null) {
            next.findViewById(com.daimajia.slider.library.R.id.description_layout).setVisibility(View.INVISIBLE);
        }
//        Log.e(TAG, "onPrepareNextItemShowInScreen called");
    }

    @Override
    public void onCurrentItemDisappear(View view) {
//        Log.e(TAG, "onCurrentItemDisappear called");
    }

    @Override
    public void onNextItemAppear(View view) {

        View descriptionLayout = view.findViewById(com.daimajia.slider.library.R.id.description_layout);
        if (descriptionLayout != null) {
            view.findViewById(com.daimajia.slider.library.R.id.description_layout).setVisibility(View.VISIBLE);
//            ValueAnimator animator = ObjectAnimator.ofFloat(
//                    descriptionLayout, "y", -descriptionLayout.getHeight(),
//                    0).setDuration(500);
//            animator.start();
            new BounceInAnimator().animate(descriptionLayout);
//            new StandUpAnimator().animate(descriptionLayout);
        }
//        Log.e(TAG, "onCurrentItemDisappear called");
    }
}