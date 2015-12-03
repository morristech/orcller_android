package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Page;
import com.orcller.app.orcller.proxy.AlbumDataProxy;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/19/15.
 */
public class AlbumFlipView extends PSFrameLayout implements FlipView.FlipViewDelegate {
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
    private View background;
    private PSFrameLayout container;
    private ProgressBar progressBar;
    private Delegate delegate;
    private List<FlipView> visibleViews;
    private FlipView targetFlipView;
    private Album model;
    private AlbumPageCountView pageCountView;

    public AlbumFlipView(Context context) {
        super(context);
    }

    public AlbumFlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumFlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
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
        allowsShowPageCount = true;
        pageIndex = -1;
        imageLoadType = MediaView.ImageLoadType.LowResolution.getValue();
        visibleViews = new ArrayList<FlipView>();
        background = new View(context);
        container = new PSFrameLayout(context);
        pageCountView = new AlbumPageCountView(context);
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);

        background.setBackgroundColor(Color.BLACK);
        addView(background);
        addView(container, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void setUpSubviews(Context context) {
        readyPages();
        EventBus.getDefault().register(this, VideoMediaView.Event.class);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                xPanningOffset = event.getRawX();

                pause();
                loadRemainPages();
                break;

            case MotionEvent.ACTION_UP:
                if (targetFlipView != null) {
                    FlipView.Direction originDirection = FlipView.rotationToDirection(originRotation);
                    FlipView.Direction direction = targetFlipView.getRotationY() < -90 ? FlipView.Direction.Left : FlipView.Direction.Right;
                    final AlbumFlipView self = this;

                    if (direction.equals(originDirection)) {
                        Runnable rotateComplete = new Runnable() {
                            @Override
                            public void run() {
                                if (delegate != null)
                                    delegate.onCancelPanning(self);
                            }
                        };

                        if (!targetFlipView.rotateAnimated(FlipView.directionToRotation(direction), rotateComplete))
                            rotateComplete.run();
                    } else {
                        if (targetFlipView.getRotationY() > -90)
                            targetFlipView.doFlip(FlipView.Direction.Left, new DecelerateInterpolator());
                        else
                            targetFlipView.doFlip(FlipView.Direction.Right, new DecelerateInterpolator());
                    }
                } else {
                    FlipView flipView = xPanningOffset > pageWidth ? getSelectedFlipView() : visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS - 1);
                    onTap(flipView, flipView.getCurrentPageView());
                }

                xPanningOffset = 0;
                targetFlipView = null;
                return false;

            case MotionEvent.ACTION_MOVE:
                float dx = xPanningOffset - event.getRawX();

                if (dx == 0)
                    break;

                if (targetFlipView == null) {
                    if (delegate != null)
                        delegate.onStartPanning(this);

                    targetFlipView = dx > 0 ? getSelectedFlipView() : visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS - 1);
                    originRotation = targetFlipView.getRotationY();
                    targetFlipView.bringToFront();
                    targetFlipView.invalidate();
                }

                if (targetFlipView != null) {
                    float mx = pageWidth * 0.65f;
                    float rotation = Math.min(0, Math.max(-180, originRotation + (180 * dx / mx * -1)));
                    targetFlipView.rotate(rotation);
                }
                break;
        }

        return true;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

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

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public Album getModel() {
        return model;
    }

    public void setModel(Album model) {
        if (ObjectUtils.equals(this.model, model))
            return;

        this.model = model;
        slideFinished = false;
        shouldLoadPages = true;

        container.setVisibility(INVISIBLE);
        pause();
        invalidateProperties();
    }

    public void loadRemainPages() {
        if (model == null || dataLoading || model.pages.count >= model.pages.total_count)
            return;

        dataLoading = true;
        final AlbumFlipView self = this;
        addView(progressBar);

        if (delegate != null)
            delegate.onStartLoadRemainPages(this);

        AlbumDataProxy.getDefault().remainPages(model, new AlbumDataProxy.CompleteHandler() {
            @Override
            public void onComplete(boolean isSuccess) {
                dataLoading = false;
                removeView(progressBar);

                if (delegate != null)
                    delegate.onLoadRemainPages(self);
            }
        });
    }

    public PageView getPageViewWithIndex(int index) {
        Page model = this.model.pages.getPageAtIndex(index);

        if (model != null) {
            PageView pageView;

            for (int i=0; i<visibleViews.size(); i++) {
                FlipView flipView = visibleViews.get(i);
                pageView = flipView.getPageView(model);

                if (pageView != null)
                    return pageView;
            }
        }

        return null;
    }

    public void pause() {
        pause(true);
    }

    public void pause(boolean callable) {
        if (!playing)
            return;

        if (targetFlipView != null) {
            targetFlipView.rotate(targetFlipView.getRotationY() < -90 ? -180 : 0);
            targetFlipView.animate().cancel();
        }

        targetFlipView = null;
        playing = false;

        if (callable && delegate != null)
            delegate.onPause(this);
    }

    public void play() {
        if (playing || model == null)
            return;

        playing = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                flipRight();
            }
        }, 1500);
    }

    public void reload() {
        pageIndexChanged();
    }

    public void stop() {
        if (!playing)
            return;

        pause();
        container.setVisibility(VISIBLE);
        setPageIndex(model.default_page_index);

        if (delegate != null)
            delegate.onStop(this);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onChangeDirection(FlipView view, FlipView.Direction direction) {
        updatePageCountView();
    }

    public void onCompleteImageLoad(FlipView view, PageView pageView) {
    }

    public void onError(FlipView view, PageView pageView) {
    }

    public void onTap(FlipView view, PageView pageView) {
        if (!allowsAutoSlide) {
            pause();
            loadRemainPages();
        }

        if (delegate != null)
            delegate.onTap(this, view, pageView);
    }

    public void willChangeDirection(FlipView view, FlipView.Direction direction, int duration, Interpolator interpolator) {
        FlipView reuseView;
        FlipView flipView;

        if (direction.equals(FlipView.Direction.Left)) {
            pageIndex--;
            reuseView = visibleViews.get(visibleViews.size() - 1);

            visibleViews.remove(reuseView);
            visibleViews.add(0, reuseView);

            for (int i=visibleViews.size() - 1; i>=0; i--) {
                flipView = visibleViews.get(i);

                if (flipView.getCurrentPageView().hasVideoMediaView())
                    flipView.getCurrentPageView().getVideoMediaView().pause();

                if (i <= 1) {
                    int currentIndex = pageIndex - (2 - i);

                    flipView.setDirection(FlipView.Direction.Left);
                    flipView.setImageLoadType(imageLoadType);
                    flipView.setPages(getPages(currentIndex));
                    container.moveChildToBack(flipView);
                } else {
                    flipView.bringToFront();
                    flipView.invalidate();
                }

                flipView.setVisibility(flipView.getPages().size() < 1 ? INVISIBLE : VISIBLE);
            }
        } else {
            pageIndex++;
            reuseView = visibleViews.get(0);

            visibleViews.remove(reuseView);
            visibleViews.add(reuseView);

            int startIndex = visibleViews.size() - 2;

            for (int i=0; i<visibleViews.size(); i++) {
                flipView = visibleViews.get(i);

                if (flipView.getCurrentPageView().hasVideoMediaView())
                    flipView.getCurrentPageView().getVideoMediaView().pause();

                if (i >= startIndex) {
                    int currentIndex = pageIndex + (i - startIndex) + 1;

                    flipView.setDirection(FlipView.Direction.Right);
                    flipView.setImageLoadType(imageLoadType);
                    flipView.setPages(getPages(currentIndex));
                    container.moveChildToBack(flipView);
                } else {
                    flipView.bringToFront();
                    flipView.invalidate();
                }

                flipView.setVisibility(flipView.getPages().size() < 1 ? INVISIBLE : VISIBLE);
            }
        }

        if (targetFlipView != null) {
            targetFlipView.bringToFront();
            targetFlipView.invalidate();
        }

        alignContainer(pageIndex, duration, interpolator, true);

        if (delegate != null)
            delegate.onChangePageIndex(this, pageIndex);
    }

    public void onEventMainThread(Object event) {
        if (event instanceof VideoMediaView.Event &&
                ((VideoMediaView.Event) event).getType() == VideoMediaView.Event.DID_START_VIDEO_PLAYING)
            pause();
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

    private void flipRight() {
        if (!playing)
            return;

        targetFlipView = getSelectedFlipView();
        originRotation = targetFlipView.getRotationY();

        targetFlipView.bringToFront();
        targetFlipView.invalidate();

        targetFlipView.doFlip(FlipView.Direction.Right, new DecelerateInterpolator(), 1500, new FlipView.CompleteHandler() {
            @Override
            public void onComplete() {
                if ((getSelectedFlipView() != null && getSelectedFlipView().getPages().size() < 2)) {
                    slideFinished = true;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stop();
                        }
                    }, 1500);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            flipRight();
                        }
                    }, 1500);
                }
            }
        });
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
                                view.invalidate();
                            } else {
                                container.moveChildToBack(view);
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
            container.addView(view, 0, new ViewGroup.LayoutParams(pageWidth, pageHeight));
            visibleViews.add(view);
            view.setX(pageWidth);
        }
    }

    private void resizePages() {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = pageWidth * PAGE_COUNT;
        params.height = pageHeight + GraphicUtils.convertDpToPixel(20);

        ViewGroup.LayoutParams bgparams = background.getLayoutParams();
        bgparams.width = params.width;
        bgparams.height = pageHeight;

        for (FlipView view : visibleViews) {
            params = view.getLayoutParams();
            params.width = pageWidth;
            params.height = pageHeight;
            view.setX(pageWidth);
        }
    }

    private void updatePageCountView() {
        if (!allowsShowPageCount)
            return;

        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                final FlipView selectedFlipView = getSelectedFlipView();

                if (pageIndex == model.default_page_index &&
                        selectedFlipView != null &&
                        selectedFlipView.getPages().size() > 0) {
                    final int index = model.pages.data.indexOf(selectedFlipView.getPages().get(0));

                    Application.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            int count = model.pages.total_count - index - 1;

                            if (count > 0) {
                                pageCountView.setText("+" + String.valueOf(count));
                                pageCountView.show(selectedFlipView.getCurrentPageView());
                            } else {
                                pageCountView.hide();
                            }
                        }
                    });
                } else {
                    Application.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            pageCountView.hide();
                        }
                    });
                }
            }
        });
    }

    public interface Delegate {
        void onCancelPanning(AlbumFlipView view);
        void onChangePageIndex(AlbumFlipView view, int pageIndex);
        void onLoadRemainPages(AlbumFlipView view);
        void onPause(AlbumFlipView view);
        void onStartLoadRemainPages(AlbumFlipView view);
        void onStartPanning(AlbumFlipView view);
        void onStop(AlbumFlipView view);
        void onTap(AlbumFlipView view, FlipView flipView, PageView pageView);
    }
}
