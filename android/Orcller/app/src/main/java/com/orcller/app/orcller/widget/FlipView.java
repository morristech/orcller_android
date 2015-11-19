package com.orcller.app.orcller.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Page;

import java.util.List;

import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSView;

/**
 * Created by pisces on 11/19/15.
 */
public class FlipView extends PSView implements PageView.PageViewDelegate {
    public enum Direction {
        Left,
        Right
    }

    public static final int FLIP_DURATION = 25000;
    private boolean imageLoadTypeChanged;
    private boolean pagesChanged;
    private int imageLoadType;
    private List<Page> pages;
    private PageView frontPageView;
    private PageView backPageView;
    private ImageView shadowImageView;
    private FlipViewDelegate delegate;

    public FlipView(Context context) {
        super(context);
    }

    public FlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FlipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // ================================================================================================
    //  Overridden: PSView
    // ================================================================================================

    @Override
    protected void commitProperties() {
        if (imageLoadTypeChanged) {
            imageLoadTypeChanged = false;
            frontPageView.setImageLoadType(imageLoadType);
            backPageView.setImageLoadType(imageLoadType);
        }

        if (pagesChanged) {
            pagesChanged = false;
            render();
        }
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        frontPageView = new PageView(context);
        frontPageView.setDelegate(this);

        backPageView = new PageView(context);
        backPageView.setDelegate(this);
        backPageView.setScaleX(-1.0f);
        backPageView.setVisibility(GONE);

        shadowImageView = new ImageView(context);
        shadowImageView.setAlpha(0.7f);
        shadowImageView.setImageResource(R.drawable.img_page_shadow);
        shadowImageView.setScaleX(-1.0f);

        setPivotX(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setCameraDistance(50 * heightMeasureSpec);
        shadowImageView.getLayoutParams().height = heightMeasureSpec;
    }

    @Override
    protected void setUpSubviews(Context context) {
        addView(frontPageView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(backPageView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(shadowImageView, (int) getResources().getDimension(R.dimen.flipview_shadow_image_width), getMeasuredHeight());
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void doFlip(Direction direction) {
        doFlip(direction, null);
    }

    public void doFlip(Direction direction, Interpolator interpolator) {
        doFlip(direction, interpolator, null);
    }

    public void doFlip(Direction direction, Interpolator interpolator, CompleteHandler completeHandler) {
        doFlip(direction, interpolator, FLIP_DURATION, completeHandler);
    }

    public void doFlip(final Direction direction, Interpolator interpolator, int duration, final CompleteHandler completeHandler) {
        if (pages == null || pages.size() < 2)
            return;

        if (delegate != null)
            delegate.willChangeDirection(this, direction, duration, interpolator);

        final FlipView self = this;

        rotateAnimated(direction.equals(Direction.Left) ? 0 : -180, duration, interpolator, new Runnable() {
            @Override
            public void run() {
                if (completeHandler != null)
                    completeHandler.onComplete();

                if (delegate != null)
                    delegate.onChangeDirection(self, direction);
            }
        });
    }

    public PageView getCurrentPageView() {
        return getDirection().equals(Direction.Left) ? backPageView : frontPageView;
    }

    public FlipViewDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(FlipViewDelegate delegate) {
        this.delegate = delegate;
    }

    public Direction getDirection() {
        return getRotationX() == -180 ? Direction.Left : Direction.Right;
    }

    public void setDirection(Direction direction) {
        frontPageView.setVisibility(direction.equals(Direction.Left) ? VISIBLE : GONE);
        backPageView.setVisibility(direction.equals(Direction.Right) ? VISIBLE : GONE);
        setRotationY(direction.equals(Direction.Left) ? -180 : 0);
    }

    public int getImageLoadType() {
        return imageLoadType;
    }

    public void setImageLoadType(int imageLoadType) {
        if (imageLoadType == this.imageLoadType)
            return;

        this.imageLoadType = imageLoadType;
        imageLoadTypeChanged = true;

        invalidateProperties();
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        if (pages.equals(this.pages))
            return;

        this.pages = pages;
        pagesChanged = true;

        if (frontPageView.hasVideoMediaView())
            frontPageView.getVideoMediaView().stop();

        if (backPageView.hasVideoMediaView())
            backPageView.getVideoMediaView().stop();

        invalidateProperties();
    }

    public void rotateAnimated(float rotation) {
        rotateAnimated(rotation, FLIP_DURATION, null, null);
    }

    public void rotateAnimated(float rotation, Runnable runnable) {
        rotateAnimated(rotation, FLIP_DURATION, null, runnable);
    }

    public void rotateAnimated(float rotation, int duration, Runnable runnable) {
        rotateAnimated(rotation, duration, null, runnable);
    }

    public void rotateAnimated(float rotation, Interpolator interpolator) {
        rotateAnimated(rotation, FLIP_DURATION, interpolator, null);
    }

    public void rotateAnimated(float rotation, int duration, Interpolator interpolator, final Runnable runnable) {
        if (pages == null || pages.size() < 2)
            return;

        animate().cancel();
        setPageVisibility();
        animate().setDuration(duration)
                .setInterpolator(interpolator)
                .rotationYBy(rotation)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        setPageVisibility();
                    }
                })
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animate().setUpdateListener(null);
                        animate().setListener(null);
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
                })
                .start();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onCompleteImageLoad(PageView view, Drawable image) {
        if (delegate != null)
            delegate.onCompleteImageLoad(this, view, image);
    }

    public void onError(PageView view) {
        if (delegate != null)
            delegate.onError(this, view);
    }

    public void onTap(PageView view) {
        if (delegate != null)
            delegate.onTap(this, view);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void render() {
        frontPageView.setModel(pages.size() > 0 ? pages.get(0) : null);
        backPageView.setModel(pages.size() > 1 ? pages.get(1) : null);
    }

    private void setPageVisibility() {
        frontPageView.setVisibility(getRotationY() <= -90 ? GONE : VISIBLE);
        backPageView.setVisibility(getRotationY() > -90 ? GONE : VISIBLE);
    }

    public interface CompleteHandler {
        void onComplete();
    }

    public interface FlipViewDelegate {
        void onChangeDirection(FlipView view, Direction direction);
        void onCompleteImageLoad(FlipView view, PageView pageView, Drawable image);
        void onError(FlipView view, PageView pageView);
        void onTap(FlipView view, PageView pageView);
        void willChangeDirection(FlipView view, Direction direction, int duration, Interpolator interpolator);
    }
}
