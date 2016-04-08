package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.orcller.app.orcller.model.Album;

import java.util.List;

import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/19/15.
 */
public class AlbumFlipView extends PSFrameLayout {
    public static final int CENTER_INDEX_OF_VISIBLE_VIEWS = 2;
    public static final int COUNT_OF_VISIBLE_VIEWS = 5;
    public static final int PAGE_COUNT = 2;

    private boolean allowsShowPageCount;
    private boolean playing;
    private boolean shouldResizePages;
    private int imageLoadType;
    private int pageHeight;
    private int pageWidth;
    private int pageIndex = -1;
    private int slideDuration = 1500;
    private float originRotation;
    private PointF startPoint;
    private List<FlipView> visibleViews;
    private Delegate delegate;
    private Album model;
    private View background;
    private PSFrameLayout container;
    private FlipView targetFlipView;
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
//
//    @Override
//    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        allowsShowPageCount = true;
//        pageIndex = -1;
//        imageLoadType = MediaView.ImageLoadType.LowResolution.value();
//        visibleViews = new ArrayList<>();
//        container = new PSFrameLayout(context);
//        pageCountView = new AlbumPageCountView(context);
//
//        setClipChildren(false);
//        setBackgroundView(new Background(context));
//        addView(container, new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));
//    }
//
//    @Override
//    protected void setUpSubviews(Context context) {
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (model == null)
//            return super.onTouchEvent(event);
//
//        switch (event.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                startPoint = new PointF(event.getRawX(), event.getRawY());
//                pause();
//                break;
//
//            case MotionEvent.ACTION_UP:
//                if (targetFlipView != null) {
//                    FlipView.Direction originDirection = FlipView.rotationToDirection(originRotation);
//                    FlipView.Direction direction = targetFlipView.getRotationY() < -90 ? FlipView.Direction.Left : FlipView.Direction.Right;
//                    final AlbumFlipView self = this;
//
//                    if (direction.equals(originDirection)) {
//                        Runnable rotateComplete = new Runnable() {
//                            @Override
//                            public void run() {
//                                if (delegate != null)
//                                    delegate.onCancelPanning(self);
//
//                                EventBus.getDefault().post(new AlbumFlipViewEvent(AlbumFlipViewEvent.CANCEL_PANNING, self));
//                            }
//                        };
//
//                        if (!targetFlipView.rotateAnimated(FlipView.directionToRotation(direction), rotateComplete))
//                            rotateComplete.run();
//                    } else {
//                        if (targetFlipView.getRotationY() > -90)
//                            targetFlipView.doFlip(FlipView.Direction.Left, new DecelerateInterpolator());
//                        else
//                            targetFlipView.doFlip(FlipView.Direction.Right, new DecelerateInterpolator());
//                    }
//                } else {
//                    FlipView flipView;
//                    if (pageIndex == 0) {
//                        flipView = getSelectedFlipView();
//                    } else if ((pageIndex * 2) + 1 > model.pages.total_count) {
//                        flipView = visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS - 1);
//                    } else {
//                        flipView = startPoint.x > pageWidth ? getSelectedFlipView() : visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS - 1);
//                    }
//
//                    if (hitTest(flipView, event)) {
//                        onTapFlipView(flipView, flipView.getCurrentPageView());
//                    } else {
//                        onTap();
//                    }
//                }
//
//                startPoint = null;
//                targetFlipView = null;
//                return false;
//
//            case MotionEvent.ACTION_MOVE:
//                float dx = startPoint.x - event.getRawX();
//                float dy = startPoint.y - event.getRawY();
//
//                if (dx == 0)
//                    break;
//
//                if (targetFlipView == null && Math.abs(dy) < Math.abs(dx) && Math.abs(dx) > 10) {
//                    targetFlipView = dx > 0 ? getSelectedFlipView() : visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS - 1);
//                    originRotation = targetFlipView.getRotationY();
//
//                    if (delegate != null)
//                        delegate.onStartPanning(this, targetFlipView);
//
//                    EventBus.getDefault().post(new AlbumFlipViewEvent(AlbumFlipViewEvent.START_PANNING, this));
//
//                    loadRemainPages();
//                    showLastPageToast();
//                }
//
//                if (targetFlipView != null) {
//                    float mx = pageWidth * 0.65f;
//                    float rotation = Math.min(0, Math.max(-180, originRotation + (180 * dx / mx * -1)));
//                    container.bringChildToFront(targetFlipView);
//                    targetFlipView.rotate(rotation);
//                }
//                break;
//        }
//        return true;
//    }
//
//    // ================================================================================================
//    //  Public
//    // ================================================================================================
//
//    public boolean isAllowsShowPageCount() {
//        return allowsShowPageCount;
//    }
//
//    public void setAllowsShowPageCount(boolean allowsShowPageCount) {
//        this.allowsShowPageCount = allowsShowPageCount;
//    }
//
//    public void setBackgroundView(View background) {
//        if (ObjectUtils.equals(background, this.background))
//            return;
//
//        if (this.background != null)
//            removeView(this.background);
//
//        this.background = background;
//
//        if (background != null)
//            addView(background, 0);
//    }
//
//    public boolean isPlaying() {
//        return playing;
//    }
//
//    public int getImageLoadType() {
//        return imageLoadType;
//    }
//
//    public void setImageLoadType(int imageLoadType) {
//        this.imageLoadType = imageLoadType;
//    }
//
//    public int getPageHeight() {
//        return pageHeight;
//    }
//
//    public void setPageHeight(int pageHeight) {
//        this.pageHeight = pageHeight;
//        shouldResizePages = true;
//    }
//
//    public int getPageWidth() {
//        return pageWidth;
//    }
//
//    public void setPageWidth(int pageWidth) {
//        this.pageWidth = pageWidth;
//        shouldResizePages = true;
//    }
//
//    public int getPageIndex() {
//        return pageIndex;
//    }
//
//    public void setPageIndex(int pageIndex) {
//        if (pageIndex == this.pageIndex ||
//                pageIndex < 0 ||
//                pageIndex > SharedObject.convertPositionToPageIndex(model.pages.total_count - 1))
//            return;
//
//        this.pageIndex = pageIndex;
//
//        pageIndexChanged();
//    }
//
//    public FrameLayout getContainer() {
//        return container;
//    }
//
//    public Delegate getDelegate() {
//        return delegate;
//    }
//
//    public void setDelegate(Delegate delegate) {
//        this.delegate = delegate;
//    }
//
//    public Album getModel() {
//        return model;
//    }
//
//    public void setModel(Album model) {
//        if (ObjectUtils.equals(this.model, model))
//            return;
//
//        this.model = model;
//
//        stop();
//
//        if (pageIndex > -1)
//            pageIndexChanged();
//    }
//
//    public int getSlideDuration() {
//        return slideDuration;
//    }
//
//    public void setSlideDuration(int slideDuration) {
//        this.slideDuration = slideDuration;
//    }
//
//    public void loadRemainPages() {
//        loadRemainPages(null);
//    }
//
//    public void loadRemainPages(final Runnable completion) {
//        if (model == null)
//            return;
//
//        if (model.pages.data.size() >= model.pages.total_count) {
//            if (completion != null)
//                completion.run();
//            return;
//        }
//
//        if (!invalidDataLoading()) {
//            final AlbumFlipView self = this;
//            ProgressBarManager.show(this);
//
//            if (delegate != null)
//                delegate.onStartLoadRemainPages(this);
//
//            AlbumDataProxy.getDefault().remainPages(model, new AlbumDataProxy.CompleteHandler() {
//                @Override
//                public void onComplete(boolean isSuccess) {
//                    endDataLoading();
//                    ProgressBarManager.hide(self);
//
//                    if (completion != null)
//                        completion.run();
//
//                    if (delegate != null)
//                        delegate.onLoadRemainPages(self);
//                }
//            });
//        }
//    }
//
//    public PageView getPageViewWithIndex(int index) {
//        Page model = this.model.pages.getPageAtIndex(index);
//
//        if (model != null) {
//            PageView pageView;
//
//            for (int i=0; i<visibleViews.size(); i++) {
//                FlipView flipView = visibleViews.get(i);
//                pageView = flipView.getPageView(model);
//
//                if (pageView != null)
//                    return pageView;
//            }
//        }
//
//        return null;
//    }
//
//    public void pause() {
//        pause(true);
//    }
//
//    public void pause(boolean callable) {
//        if (!playing)
//            return;
//
//        targetFlipView = null;
//        playing = false;
//
//        if (callable && delegate != null)
//            delegate.onPause(this);
//    }
//
//    public void play() {
//        if (playing || model == null || model.pages.total_count <= 1)
//            return;
//
//        playing = true;
//
//        if (delegate != null)
//            delegate.onPlay(this);
//
//        loadRemainPages(new Runnable() {
//            @Override
//            public void run() {
//                if (pageIndex >= SharedObject.convertPositionToPageIndex(model.pages.data.size() - 1))
//                    setPageIndex(model.default_page_index);
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        flipRight();
//                    }
//                }, 200);
//            }
//        });
//    }
//
//    public void reload() {
//        pageIndexChanged();
//    }
//
//    public void stop() {
//        stop(true);
//    }
//
//    /**
//     * FlipView.Delegate
//     */
//    public void onChangeDirection(FlipView view, FlipView.Direction direction) {
//        updatePageCountView();
//    }
//
//    public void onCompleteImageLoad(FlipView view, PageView pageView) {
//    }
//
//    public void onError(FlipView view, PageView pageView) {
//    }
//
//    public void willChangeDirection(FlipView view, FlipView.Direction direction, int duration, Interpolator interpolator) {
//        FlipView reuseView;
//        FlipView flipView;
//
//        if (direction.equals(FlipView.Direction.Left)) {
//            pageIndex--;
//            reuseView = visibleViews.get(visibleViews.size() - 1);
//
//            visibleViews.remove(reuseView);
//            visibleViews.add(0, reuseView);
//
//            for (int i=visibleViews.size() - 1; i>=0; i--) {
//                flipView = visibleViews.get(i);
//
//                if (flipView.getCurrentPageView().hasVideoMediaView())
//                    flipView.getCurrentPageView().getVideoMediaView().pause();
//
//                if (i <= 1) {
//                    int currentIndex = pageIndex - (2 - i);
//
//                    flipView.setDirection(FlipView.Direction.Left);
//                    flipView.setPages(getPages(currentIndex));
//                    container.moveChildToBack(flipView);
//                } else {
//                    container.bringChildToFront(flipView);
//                }
//
//                flipView.setVisibility(flipView.getPages().size() < 1 ? INVISIBLE : VISIBLE);
//            }
//        } else {
//            pageIndex++;
//            reuseView = visibleViews.get(0);
//
//            visibleViews.remove(reuseView);
//            visibleViews.add(reuseView);
//
//            int startIndex = visibleViews.size() - 2;
//
//            for (int i=0; i<visibleViews.size(); i++) {
//                flipView = visibleViews.get(i);
//
//                if (flipView.getCurrentPageView().hasVideoMediaView())
//                    flipView.getCurrentPageView().getVideoMediaView().pause();
//
//                if (i >= startIndex) {
//                    int currentIndex = pageIndex + (i - startIndex) + 1;
//
//                    flipView.setDirection(FlipView.Direction.Right);
//                    flipView.setPages(getPages(currentIndex));
//                    container.moveChildToBack(flipView);
//                } else {
//                    container.bringChildToFront(flipView);
//                }
//
//                flipView.setVisibility(flipView.getPages().size() < 1 ? INVISIBLE : VISIBLE);
//            }
//        }
//
//        if (targetFlipView != null) {
//            container.bringChildToFront(targetFlipView);
//        }
//
//        alignContainer(pageIndex, duration, interpolator, true);
//
//        if (delegate != null)
//            delegate.onChangePageIndex(this, pageIndex);
//
//        EventBus.getDefault().post(new AlbumFlipViewEvent(AlbumFlipViewEvent.PAGE_INDEX_CHANGE, this));
//    }
//
//    public void onEventMainThread(Object event) {
//        if (event instanceof VideoMediaView.Event &&
//                ((VideoMediaView.Event) event).getType() == VideoMediaView.Event.DID_START_VIDEO_PLAYING)
//            pause();
//    }
//
//    // ================================================================================================
//    //  Private
//    // ================================================================================================
//
//    private void alignContainer(int pageIndex, boolean animated) {
//        alignContainer(pageIndex, FlipView.FLIP_DURATION, new DecelerateInterpolator(), animated);
//    }
//
//    private void alignContainer(int pageIndex, int duration, Interpolator interpolator, boolean animated) {
//        if (pageWidth < 1 || model == null)
//            return;
//
//        int x;
//        int cw = pageWidth * PAGE_COUNT;
//
//        if (pageIndex == 0)
//            x = (getWidth() - cw)/2 - (pageWidth/2);
//        else if ((pageIndex * 2) + 1 > model.pages.count)
//            x = (getWidth() - cw)/2 + (pageWidth/2);
//        else
//            x = (getWidth() - cw)/2;
//
//        if (animated) {
//            container.animate()
//                    .setDuration(duration - 50)
//                    .setInterpolator(interpolator)
//                    .x(x)
//                    .start();
//        } else {
//            container.setX(x);
//        }
//    }
//
//    private void flipRight() {
//        if (!playing)
//            return;
//
//        targetFlipView = getSelectedFlipView();
//        originRotation = targetFlipView.getRotationY();
//
//        container.bringChildToFront(targetFlipView);
//        targetFlipView.doFlip(FlipView.Direction.Right, new LinearInterpolator(), 1000, new FlipView.CompleteHandler() {
//            @Override
//            public void onComplete() {
//                if ((getSelectedFlipView() != null && getSelectedFlipView().getPages().size() < 2)) {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            stop();
//                        }
//                    }, slideDuration);
//                } else {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            flipRight();
//                        }
//                    }, slideDuration);
//                }
//            }
//        });
//    }
//
//    private List<Page> getPages(int pageIndex) {
//        int frontIndex = pageIndex * PAGE_COUNT;
//        int backIndex = frontIndex + 1;
//        Page p1 = frontIndex < model.pages.count ? model.pages.getPageAtIndex(frontIndex) : null;
//        Page p2 = backIndex < model.pages.count ? model.pages.getPageAtIndex(backIndex) : null;
//
//        List<Page> pages = new ArrayList<Page>();
//        if (p1 != null)     pages.add(p1);
//        if (p2 != null)     pages.add(p2);
//
//        return pages;
//    }
//
//    private FlipView getSelectedFlipView() {
//        return visibleViews.size() > CENTER_INDEX_OF_VISIBLE_VIEWS ?
//                visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS) : null;
//    }
//
//    private boolean hitTest(FlipView view, MotionEvent event) {
//        int addendX = view.getDirection().equals(FlipView.Direction.Left) ? -view.getWidth() : 0;
//        float xMin = container.getX() + view.getX() + addendX;
//        float xMax = container.getX() + view.getX() + addendX + view.getWidth();
//        float yMin = container.getY() + view.getY();
//        float yMax = container.getY() + view.getY() + view.getHeight();
//        return event.getX() >= xMin &&
//                event.getX() <= xMax &&
//                event.getY() >= yMin &&
//                event.getY() <= yMax;
//    }
//
//    private void onTap() {
//        pause();
//
//        if (delegate != null)
//            delegate.onTap(this);
//    }
//
//    private void onTapFlipView(FlipView view, PageView pageView) {
//        pause();
//
//        if (delegate != null)
//            delegate.onTapFlipView(this, view, pageView);
//    }
//
//    private void pageIndexChanged() {
//        if (model == null || pageIndex < 0)
//            return;
//
//        final AlbumFlipView self = this;
//
//        Application.runOnBackgroundThread(new Runnable() {
//            @Override
//            public void run() {
//                int startPageIndex = pageIndex - 2;
//
//                for (int i=0; i<COUNT_OF_VISIBLE_VIEWS; i++) {
//                    final FlipView.Direction direction = i < CENTER_INDEX_OF_VISIBLE_VIEWS ? FlipView.Direction.Left : FlipView.Direction.Right;
//                    final List<Page> pages = getPages(startPageIndex + i);
//                    final int idx = i;
//
//                    Application.runOnMainThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            FlipView view;
//                            if (visibleViews.size() > idx) {
//                                view = visibleViews.get(idx);
//                            } else {
//                                view = new FlipView(getContext());
//                                view.setDelegate(self);
//                                view.setTag(idx);
//                                container.addView(view, 0);
//                                visibleViews.add(view);
//                            }
//
//                            if (shouldResizePages) {
//                                view.setDirection(FlipView.Direction.Right);
//                                view.setVisibility(VISIBLE);
//                                view.getLayoutParams().width = pageWidth;
//                                view.getLayoutParams().height = pageHeight;
//                                view.setX(pageWidth);
//                            }
//
//                            if (!view.equals(targetFlipView))
//                                view.setDirection(direction);
//
//                            view.getShadowImageView().setVisibility(model.pages.total_count < 2 ? GONE : VISIBLE);
//                            view.setImageLoadType(imageLoadType);
//                            view.setPages(pages);
//                            view.setVisibility(pages.size() < 1 ? INVISIBLE : VISIBLE);
//
//                            if (view.getDirection().equals(FlipView.Direction.Left)) {
//                                container.bringChildToFront(view);
//                            } else {
//                                container.moveChildToBack(view);
//                            }
//                        }
//                    });
//                }
//
//                Application.runOnMainThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (shouldResizePages) {
//                            shouldResizePages = false;
//                            container.getLayoutParams().width = pageWidth * PAGE_COUNT;
//                            container.getLayoutParams().height = pageHeight;
//                        }
//
//                        alignContainer(pageIndex, false);
//                        updatePageCountView();
//                    }
//                });
//            }
//        });
//    }
//
//    private void stop(boolean resetPages) {
//        if (!playing)
//            return;
//
//        if (targetFlipView != null) {
//            targetFlipView.rotate(targetFlipView.getRotationY() < -90 ? -180 : 0);
//            targetFlipView.animate().cancel();
//        }
//
//        pause();
//
//        if (resetPages)
//            setPageIndex(model.default_page_index);
//
//        if (delegate != null)
//            delegate.onStop(this);
//    }
//
//    private void showLastPageToast() {
//        if (targetFlipView == null)
//            return;
//
//        if (targetFlipView.getDirection().equals(FlipView.Direction.Right) &&
//                (targetFlipView.getCurrentPageView().getModel() == null || ObjectUtils.equals(targetFlipView.getCurrentPageView().getModel(), model.pages.getLastPage())))
//            Toast.makeText(Application.applicationContext(), R.string.m_hint_album_last_page, Toast.LENGTH_LONG).show();
//    }
//
//    private void updatePageCountView() {
//        if (!allowsShowPageCount)
//            return;
//
//        Application.runOnBackgroundThread(new Runnable() {
//            @Override
//            public void run() {
//                final FlipView selectedFlipView = getSelectedFlipView();
//
//                if (pageIndex == model.default_page_index &&
//                        selectedFlipView != null &&
//                        selectedFlipView.getPages().size() > 0) {
//                    final int index = model.pages.data.indexOf(selectedFlipView.getPages().get(0));
//
//                    Application.runOnMainThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            int count = model.pages.total_count - index - 1;
//
//                            if (count > 0) {
//                                pageCountView.setText("+" + String.valueOf(count));
//                                pageCountView.show(selectedFlipView.getCurrentPageView());
//                            } else {
//                                pageCountView.hide();
//                            }
//                        }
//                    });
//                } else {
//                    Application.runOnMainThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            pageCountView.hide();
//                        }
//                    });
//                }
//            }
//        });
//    }
//
//    // ================================================================================================
//    //  Class: Background
//    // ================================================================================================
//
//    private class Background extends View {
//        private Path path;
//        private Paint backgroundPaint;
//        private Paint paint;
//
//        public Background(Context context) {
//            super(context);
//
//            path = new Path();
//            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//
//            backgroundPaint.setColor(getContext().getResources().getColor(R.color.theme_white_accent));
//            backgroundPaint.setStyle(Paint.Style.FILL);
//
//            paint.setColor(getContext().getResources().getColor(R.color.border_hairline_lightgray));
//            paint.setStrokeWidth(GraphicUtils.convertDpToPixel(1));
//            paint.setStyle(Paint.Style.STROKE);
//        }
//
//        @Override
//        public void onDraw(Canvas canvas) {
//            super.onDraw(canvas);
//
//            int w = getWidth();
//            int h = getHeight();
//
//            canvas.drawRect(0, 0, w, h, backgroundPaint);
//            path.reset();
//            path.moveTo(0, h);
//            path.lineTo(w, h);
//            path.close();
//            canvas.drawPath(path, paint);
//        }
//    }

    // ================================================================================================
    //  Class: Delegate
    // ================================================================================================

    public interface Delegate {
        void onCancelPanning(AlbumFlipView view);
        void onChangePageIndex(AlbumFlipView view, int pageIndex);
        void onLoadRemainPages(AlbumFlipView view);
        void onPlay(AlbumFlipView view);
        void onPause(AlbumFlipView view);
        void onStartLoadRemainPages(AlbumFlipView view);
        void onStartPanning(AlbumFlipView view, FlipView flipView);
        void onStop(AlbumFlipView view);
        void onTap(AlbumFlipView view);
        void onTapFlipView(AlbumFlipView view, FlipView flipView, PageView pageView);
    }
}
