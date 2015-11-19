package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Page;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSView;

/**
 * Created by pisces on 11/19/15.
 */
public class AlbumFlipView extends PSView implements FlipView.FlipViewDelegate, View.OnTouchListener {

    public static final int CENTER_INDEX_OF_VISIBLE_VIEWS = 2;
    public static final int COUNT_OF_VISIBLE_VIEWS = 5;
    public static final int PAGE_COUNT = 2;

    private boolean allowsAutoSlide;
    private boolean allowsShowPageCount;
    private boolean dataLoading;
    private boolean pageOpened;
    private boolean playing;
    private boolean slideFinished;
    private boolean shouldLoadPages;
    private boolean shouldResizePages;
    private int imageLoadType;
    private int pageHeight;
    private int pageWidth;
    private int pageIndex;
    private float originRotation;
    private float xPanningOffset;
    private FrameLayout container;
    private AlbumFlipViewDelegate delegate;
    private List<FlipView> visibleViews;
    private FlipView targetFlipView;
    private Album model;
//    AlbumPageCountView *pageCountView;

    public AlbumFlipView(Context context) {
        super(context);
    }

    public AlbumFlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumFlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AlbumFlipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // ================================================================================================
    //  Overridden: PSView
    // ================================================================================================

    @Override
    protected void commitProperties() {
        if (shouldLoadPages) {
            shouldLoadPages = false;
            pageIndexChanged();
        }

        if (shouldResizePages) {
            shouldResizePages = false;
            resizePages();
        }
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        visibleViews = new ArrayList<FlipView>();
        allowsShowPageCount = true;
        pageIndex = -1;
        imageLoadType = MediaView.ImageLoadType.LowResolution.getValue();
        container = new FrameLayout(context);

        setBackgroundColor(Color.BLACK);
        setOnTouchListener(this);
    }

    public boolean onTouch(View v, MotionEvent event) {
        float rotation = 0.0f;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xPanningOffset = event.getX();

                if (delegate != null)
                    delegate.onStartPanning(this);

                pause();
                loadRemainPages();
                break;

            case MotionEvent.ACTION_UP:
                rotation = targetFlipView.getRotationY();

                if (originRotation != rotation) {
                    if (rotation > -180)
                        targetFlipView.doFlip(FlipView.Direction.Left, new DecelerateInterpolator());
                    else
                        targetFlipView.doFlip(FlipView.Direction.Right, new DecelerateInterpolator());
                } else {
                    targetFlipView.rotateAnimated(rotation);
                }

                xPanningOffset = 0;
                targetFlipView = null;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                float p = event.getX();
                float dx = xPanningOffset - p;

                if (dx == 0)
                    break;

                if (targetFlipView == null) {
                    targetFlipView = dx > 0 ? getSelectedFlipView() : visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS - 1);
                    originRotation = targetFlipView.getRotationY();
                    targetFlipView.bringToFront();
                }

                float mx = pageWidth * 0.65f;
                rotation = Math.min(0, Math.max(-180, originRotation + (180 * dx / mx * -1)));
                targetFlipView.rotateAnimated(rotation, 0);
                break;
        }

        return true;
    }

    @Override
    protected void setUpSubviews(Context context) {
        addView(container);
        readyPages();
        EventBus.getDefault().register(this, VideoMediaView.VideoMediaViewEvent.class);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void loadRemainPages() {

    }

    public void pause() {

    }

    public boolean isAllowsAutoSlide() {
        return allowsAutoSlide;
    }

    public void setAllowsAutoSlide(boolean allowsAutoSlide) {
        this.allowsAutoSlide = allowsAutoSlide;
    }

    public boolean isAllowsShowPageCount() {
        return allowsShowPageCount;
    }

    public void setAllowsShowPageCount(boolean allowsShowPageCount) {
        this.allowsShowPageCount = allowsShowPageCount;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isSlideFinished() {
        return slideFinished;
    }

    public int getImageLoadType() {
        return imageLoadType;
    }

    public void setImageLoadType(int imageLoadType) {
        this.imageLoadType = imageLoadType;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
        shouldResizePages = true;

        invalidateProperties();
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
        shouldResizePages = true;

        invalidateProperties();
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        if (pageIndex == this.pageIndex ||
                pageIndex < 0 ||
                pageIndex > model.pages.total_count/PAGE_COUNT)
            return;

        this.pageIndex = pageIndex;
        shouldLoadPages = true;

        invalidateProperties();
    }

    public FrameLayout getContainer() {
        return container;
    }

    public AlbumFlipViewDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(AlbumFlipViewDelegate delegate) {
        this.delegate = delegate;
    }

    public Album getModel() {
        return model;
    }

    public void setModel(Album model) {
        if (Model.equasl(model, this.model))
            return;

        this.model = model;
        slideFinished = false;
        shouldLoadPages = true;

        container.setVisibility(INVISIBLE);
        pause();
        invalidateProperties();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onChangeDirection(FlipView view, FlipView.Direction direction) {

    }

    public void onCompleteImageLoad(FlipView view, PageView pageView, Drawable image) {

    }

    public void onError(FlipView view, PageView pageView) {

    }

    public void onTap(FlipView view, PageView pageView) {

    }

    public void willChangeDirection(FlipView view, FlipView.Direction direction, int duration, Interpolator interpolator) {
        FlipView reuseView;

        if (direction.equals(FlipView.Direction.Left)) {
            pageIndex--;
            reuseView = visibleViews.get(visibleViews.size() - 1);

            visibleViews.remove(reuseView);
            visibleViews.add(0, reuseView);

            for (int i=visibleViews.size() - 1; i>=0; i--) {
                FlipView view = visibleViews.get(i);

                if (view.getCurrentPageView().hasVideoMediaView())
                    view.getCurrentPageView().getVideoMediaView().pause();

                if (i <= 1) {
                    int currentIndex = pageIndex - (2 - i);

                    view.setDirection(FlipView.Direction.Left);
                    view.setImageLoadType(imageLoadType);
                    view.setPages(getPages(currentIndex));

//                    [self.containerView sendSubviewToBack:view];
                } else {
                    view.bringToFront();
                }

                view.setVisibility(view.getPages().size() < 1 ? INVISIBLE : VISIBLE);
            }
        } else {
            _pageIndex++;

            reuseView = visibleViews.firstObject;

            [visibleViews removeObject:reuseView];
            [visibleViews addObject:reuseView];

            NSUInteger startIndex = visibleViews.count - 2;

            for (NSUInteger i=0; i<visibleViews.count; i++) {
                FlipView *view = visibleViews[i];

                [view.currentPageView.videoMediaView pause];

                if (i >= startIndex) {
                    NSUInteger currentIndex = self.pageIndex + (i - startIndex) + 1;
                    view.direction = FlipViewDirectionRight;
                    view.imageLoadType = self.imageLoadType;
                    view.pages = [self pageModelsWithPageIndex:currentIndex];

                    [self.containerView sendSubviewToBack:view];
                } else {
                    [self.containerView bringSubviewToFront:view];
                }

                view.hidden = view.pages.count < 1;
            }
        }

        if (targetFlipView)
        [self.containerView bringSubviewToFront:targetFlipView];

        [self alignWithPageIndex:self.pageIndex duration:duration options:options animated:YES];

        if ([self.delegate respondsToSelector:@selector(albumFlipView:didChangePageIndex:)])
        [self.delegate albumFlipView:self didChangePageIndex:self.pageIndex];
    }

    public void onEventMainThread(Object event) {

    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void alignContainer(int pageIndex, boolean animated) {
        alignContainer(pageIndex, FlipView.FLIP_DURATION, new DecelerateInterpolator(), animated);
    }

    private void alignContainer(int pageIndex, int duration, Interpolator interpolator, boolean animated) {
        int x = 0;
        if (pageIndex == 0)
            x = (getMeasuredWidth() - container.getMeasuredWidth())/2 - (pageWidth/2);
        else if ((pageIndex * 2) + 1 > model.pages.count)
            x = (getMeasuredWidth() - container.getMeasuredWidth())/2 + (pageWidth/2);
        else
            x = (getMeasuredWidth() - container.getMeasuredWidth())/2;

        if (animated) {
            container.animate()
                    .setDuration(duration - 50)
                    .setInterpolator(interpolator)
                    .x(x)
                    .start();
        } else {
            container.setX(x);
        }
    }

    private List<Page> getPages(int pageIndex) {
        int frontIndex = pageIndex * PAGE_COUNT;
        int backIndex = frontIndex + 1;
        Page p1 = frontIndex < model.pages.count ? model.pages.getPageAtIndex(frontIndex) : null;
        Page p2 = backIndex < model.pages.count ? model.pages.getPageAtIndex(backIndex) : null;

        List<Page> pages = new ArrayList<Page>();
        if (p1 != null)     pages.add(p1);
        if (p2 != null)     pages.add(p2);

        return pages;
    }

    private FlipView getSelectedFlipView() {
        return visibleViews.size() > CENTER_INDEX_OF_VISIBLE_VIEWS ?
                visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS) : null;
    }

    private void pageIndexChanged() {
        if (model == null || pageIndex < 0)
            return;

        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                int startPageIndex = pageIndex - 2;

                for (int i=0; i<visibleViews.size(); i++) {
                    final FlipView view = visibleViews.get(i);
                    final FlipView.Direction direction = i < CENTER_INDEX_OF_VISIBLE_VIEWS ? FlipView.Direction.Left : FlipView.Direction.Right;
                    final List<Page> pages = getPages(startPageIndex + i);

                    Application.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!view.equals(targetFlipView))
                                view.setDirection(direction);

                            view.setPages(pages);
                            view.setVisibility(pages.size() < 1 ? INVISIBLE : VISIBLE);

                            if (view.getDirection().equals(FlipView.Direction.Left)) {
                                view.bringToFront();
                            }
                        }
                    });
                }

                Application.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        alignContainer(pageIndex, false);
                        updatePageCountView();

                        if (container.getVisibility() == INVISIBLE) {
                            container.setVisibility(VISIBLE);
                            container.setAlpha(0);
                            container.animate()
                                    .setDuration(500)
                                    .alpha(1)
                                    .start();
                        }
                    }
                });
            }
        });
    }

    private void readyPages() {
        for (int i=0; i<COUNT_OF_VISIBLE_VIEWS; i++) {
            FlipView view = new FlipView(getContext());
            view.setDelegate(this);
            view.setDirection(FlipView.Direction.Right);
            view.setImageLoadType(imageLoadType);
            view.setTag(i);
            container.addView(view, 0, new LayoutParams(pageWidth, pageHeight));
            visibleViews.add(view);
            view.setX(pageWidth);
        }
    }

    private void resizePages() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) container.getLayoutParams();
        params.height = pageHeight;
        params.width = pageWidth * PAGE_COUNT;
        container.setLayoutParams(params);

        for (FlipView view : visibleViews) {
            params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.width = pageWidth;
            params.height = pageHeight;
            view.setLayoutParams(params);
            view.setX(pageWidth);
        }
    }

    private void updatePageCountView() {

    }

    public interface AlbumFlipViewDelegate {
        void onStartPanning(AlbumFlipView view);
    }
}
