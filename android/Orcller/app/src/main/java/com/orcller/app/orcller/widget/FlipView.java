package com.orcller.app.orcller.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.animator.AnimationOptions;
import com.orcller.app.orcller.model.Page;

import pisces.psfoundation.utils.ObjectUtils;

/**
 * Created by pisces on 11/19/15.
 */
public class FlipView extends TemplateView {
    public static final int FLIP_DURATION = 250;
    private PageView frontPageView;
    private PageView backPageView;
    private ImageView shadowImageView;

    public FlipView(Context context) {
        super(context);
    }

    public FlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        frontPageView = pageView;

        backPageView = new PageView(context);
        backPageView.setDelegate(this);
        backPageView.setScaleX(-1.0f);
        backPageView.setVisibility(INVISIBLE);

        shadowImageView = new ImageView(context);
        shadowImageView.setAlpha(0.7f);
        shadowImageView.setImageResource(R.drawable.img_page_shadow);
        shadowImageView.setScaleX(-1.0f);
        shadowImageView.setAdjustViewBounds(true);
        shadowImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        setPivotX(0);
        addView(backPageView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(shadowImageView, (int) getResources().getDimension(R.dimen.flipview_shadow_image_width), getMeasuredHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setCameraDistance(45 * getMeasuredHeight());
        shadowImageView.getLayoutParams().height = getMeasuredHeight();
    }

    @Override
    public PageView getCurrentPageView() {
        return getDirection().equals(Direction.Left) ? backPageView : frontPageView;
    }

    @Override
    public PageView getPageView(Page model) {
        if (ObjectUtils.equals(frontPageView.getModel(), model))
            return frontPageView;
        if (ObjectUtils.equals(backPageView.getModel(), model))
            return backPageView;
        return null;
    }

    @Override
    protected void imageLoadTypeChanged() {
        frontPageView.setImageLoadType(getImageLoadType());
        backPageView.setImageLoadType(getImageLoadType());
    }

    @Override
    protected void pagesChanged() {
        if (frontPageView.hasVideoMediaView())
            frontPageView.getVideoMediaView().stop();

        if (backPageView.hasVideoMediaView())
            backPageView.getVideoMediaView().stop();

        if (getPages() != null) {
            frontPageView.setModel(getPages().size() > 0 ? getPages().get(0) : null);
            backPageView.setModel(getPages().size() > 1 ? getPages().get(1) : null);
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static float directionToRotation(AnimationOptions.Direction direction) {
        return direction.equals(AnimationOptions.Direction.Left) ? -180 : 0;
    }

    public static AnimationOptions.Direction rotationToDirection(float rotation) {
        return rotation == -180 ? AnimationOptions.Direction.Left : AnimationOptions.Direction.Right;
    }

    public ImageView getShadowImageView() {
        return shadowImageView;
    }

    public AnimationOptions.Direction getDirection() {
        return getRotationY() == -180 ? AnimationOptions.Direction.Left : AnimationOptions.Direction.Right;
    }

    public void setDirection(AnimationOptions.Direction direction) {
        frontPageView.setVisibility(direction.equals(AnimationOptions.Direction.Left) ? INVISIBLE : VISIBLE);
        backPageView.setVisibility(direction.equals(AnimationOptions.Direction.Right) ? INVISIBLE : VISIBLE);
        setRotationY(direction.equals(AnimationOptions.Direction.Left) ? -180 : 0);
    }

    public void rotate(float rotation) {
        if (getPages() == null || getPages().size() < 2)
            return;

        animate().cancel();
        setRotationY(rotation);
        setPageVisibility();
    }

    public boolean rotateAnimated(float rotation) {
        return rotateAnimated(rotation, FLIP_DURATION, null, null);
    }

    public boolean rotateAnimated(float rotation, int duration) {
        return rotateAnimated(rotation, duration, null, null);
    }

    public boolean rotateAnimated(float rotation, Runnable runnable) {
        return rotateAnimated(rotation, FLIP_DURATION, null, runnable);
    }

    public boolean rotateAnimated(float rotation, int duration, Runnable runnable) {
        return rotateAnimated(rotation, duration, null, runnable);
    }

    public boolean rotateAnimated(float rotation, Interpolator interpolator) {
        return rotateAnimated(rotation, FLIP_DURATION, interpolator, null);
    }

    public boolean rotateAnimated(float rotation, int duration, Interpolator interpolator, final Runnable runnable) {
        if (getPages() == null || getPages().size() < 2)
            return false;

        setPageVisibility();
        animate().cancel();

        ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setPageVisibility();
            }
        };

        Animator.AnimatorListener listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    animate().setUpdateListener(null);
                    animate().setListener(null);
                }

                setPageVisibility();

                if (runnable != null)
                    runnable.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            animate().setDuration(duration)
                    .setInterpolator(interpolator)
                    .rotationY(rotation)
                    .setUpdateListener(updateListener)
                    .setListener(listener)
                    .start();
        } else {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, View.ROTATION_Y, rotation)
                    .setDuration(duration);
            objectAnimator.setInterpolator(interpolator);
            objectAnimator.addListener(listener);
            objectAnimator.addUpdateListener(updateListener);
            objectAnimator.start();
        }

        return true;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setPageVisibility() {
        frontPageView.setVisibility(getRotationY() <= -90 ? INVISIBLE : VISIBLE);
        backPageView.setVisibility(getRotationY() > -90 ? INVISIBLE : VISIBLE);
    }
}
