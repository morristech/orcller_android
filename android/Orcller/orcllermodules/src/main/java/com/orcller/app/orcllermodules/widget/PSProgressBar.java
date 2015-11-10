package com.orcller.app.orcllermodules.widget;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;

import com.orcller.app.orcllermodules.R;

/**
 * Created by pisces on 11/9/15.Ø
 */
public class PSProgressBar extends ProgressBar {
    private boolean showing;
    private Animation animation;

    public PSProgressBar(Context context) {
        super(context, null, android.R.attr.progressBarStyleLarge);

        setIndeterminate(true);
        setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.psprogressbar_loading));

        Interpolator interpolator = new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        };

        animation = AnimationUtils.loadAnimation(context, R.anim.psprogressbar_show_ani);
        animation.setInterpolator(interpolator);
    }

    public void dismiss() {
        if (showing) {
            clearAnimation();

            ViewParent viewParent = getParent();

            if (viewParent instanceof ViewGroup)
                ((ViewGroup) viewParent).removeView(this);

            showing = false;
        }
    }

    public void show(ViewGroup viewGroup) {
        if (!showing) {
            showing = true;

            viewGroup.addView(this);
            startAnimation(animation);
        }
    }
}
