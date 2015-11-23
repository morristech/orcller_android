package com.orcller.app.orcller.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.orcller.app.orcller.model.album.ImageMedia;
import com.orcller.app.orcller.model.album.Media;

import pisces.psfoundation.model.Model;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/22/15.
 */
public class ImageMediaScrollView extends PSFrameLayout {
    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 5f;

    private boolean isScaling;
    private boolean endScalingNextUp;
    private boolean modelChanged;
    private float scale = 1;
    private PointF beginPoint = new PointF();
    private PointF panPoint = new PointF();
    private PointF touchPoint;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private ImageMedia model;
    private ImageMediaView mediaView;
    private MediaView.MediaViewDelegate mediaViewDelegate;

    public ImageMediaScrollView(Context context) {
        super(context);
    }

    public ImageMediaScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageMediaScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImageMediaScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void commitProperties() {
        if (modelChanged) {
            modelChanged = false;
            modelChanged();
        }
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
        mediaView = new ImageMediaView(context);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

        setClipChildren(false);
        mediaView.setLayoutParams(params);
        mediaView.setImageLoadType(MediaView.ImageLoadType.LowResolution.getValue() |
                MediaView.ImageLoadType.StandardResoultion.getValue());
        addView(mediaView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ViewGroup.LayoutParams params = mediaView.getLayoutParams();
        params.width = params.height = getMeasuredWidth();
        mediaView.setLayoutParams(params);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();

        if (scale != 1 && !isScaling) {
            float x = event.getRawX();
            float y = event.getRawY();

            if (action == MotionEvent.ACTION_DOWN) {
                ImageView imageView = mediaView.getImageView();
                touchPoint = new PointF(x, y);
                beginPoint.set(imageView.getTranslationX(), imageView.getTranslationY());
            } else if (action == MotionEvent.ACTION_MOVE && touchPoint != null) {
                panPoint.x = x - touchPoint.x;
                panPoint.y = y - touchPoint.y;
                pan();
            }
        }

        if (isScaling && endScalingNextUp && action == MotionEvent.ACTION_POINTER_UP) {
            endScalingNextUp = false;
            isScaling = false;
            touchPoint = null;
        }

        return true;
    }

    @Override
    protected void setUpSubviews(Context context) {
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void reset() {
        scale = 1;
        panPoint = new PointF(0, 0);
        scale(false);
        isScaling = false;
    }

    public ImageMediaView getMediaView() {
        return mediaView;
    }

    public MediaView.MediaViewDelegate getMediaViewDelegate() {
        return mediaViewDelegate;
    }

    public void setMediaViewDelegate(MediaView.MediaViewDelegate mediaViewDelegate) {
        this.mediaViewDelegate = mediaViewDelegate;

        mediaView.setDelegate(mediaViewDelegate);
    }

    public Media getModel() {
        return model;
    }

    public void setModel(ImageMedia model) {
        if (Model.equasl(this.model, model))
            return;

        this.model = model;
        modelChanged = true;

        invalidateProperties();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private PointF getTranslationPoint() {
        ImageView imageView = mediaView.getImageView();
        float xmin = (imageView.getWidth() - (imageView.getWidth() * scale))/2;
        float ymin = (imageView.getHeight() - (imageView.getHeight() * scale))/2;
        float x = Math.max(xmin, Math.min(xmin * -1, beginPoint.x + panPoint.x));
        float y = Math.max(ymin, Math.min(ymin * -1, beginPoint.y + panPoint.y));
        return new PointF(x, y);
    }

    private void modelChanged() {
        reset();
        mediaView.setModel(model);
    }

    private void pan() {
        ImageView imageView = mediaView.getImageView();
        PointF point = getTranslationPoint();
        imageView.setTranslationX(point.x);
        imageView.setTranslationY(point.y);
    }

    private void scale(boolean animated) {
        ImageView imageView = mediaView.getImageView();
        PointF point = getTranslationPoint();

        if (animated) {
            imageView.animate()
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            endScalingNextUp = true;
                            isScaling = false;
                            touchPoint = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    })
                    .scaleX(scale)
                    .scaleY(scale)
                    .translationX(point.x)
                    .translationY(point.y);
        } else {
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
            imageView.setTranslationX(point.x);
            imageView.setTranslationY(point.y);
        }
    }

    // ================================================================================================
    //  Class: ScaleListener
    // ================================================================================================

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            endScalingNextUp = true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScaling = true;
            endScalingNextUp = false;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            isScaling = true;
            endScalingNextUp = false;
            scale *= detector.getScaleFactor();
            scale = Math.min(Math.max(MIN_ZOOM, scale), MAX_ZOOM);
            scale(false);
            return true;
        }
    }

    // ================================================================================================
    //  Class: GestureListener
    // ================================================================================================

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onDown(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTap(MotionEvent e) {
            isScaling = true;
            endScalingNextUp = false;
            scale = scale > 1 ? MIN_ZOOM : MAX_ZOOM;
            scale(true);
            return true;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        public boolean onContextClick(MotionEvent e) {
            return false;
        }
    }
}
