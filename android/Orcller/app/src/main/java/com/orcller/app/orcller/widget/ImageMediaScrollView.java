package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.lang.ref.WeakReference;

import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/22/15.
 */
public class ImageMediaScrollView extends MediaView {
    private boolean scaleEnabled = true;
    private WeakReference<SubsamplingScaleImageView> scaleImageView;

    public ImageMediaScrollView(Context context) {
        super(context);
    }

    public ImageMediaScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageMediaScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: MediaView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        scaleImageView = new WeakReference<>(new SubsamplingScaleImageView(context));
        scaleImageView.get().setDoubleTapZoomScale(2f);
        setImageLoadType(ImageLoadType.StandardResoultion.getValue());
        removeView(imageView);
        addView(scaleImageView.get(), 0);
    }

    @Override
    protected void clickEnabled() {
        if (isClickEnabled())
            scaleImageView.get().setOnClickListener(this);
        else
            scaleImageView.get().setOnClickListener(null);
    }

    @Override
    protected void loadImages() {
        final MediaView self = this;

        if (delegate != null)
            delegate.onStartImageLoad(this);

        loadImages(new CompleteHandler() {
            @Override
            public void onComplete() {
                if (delegate != null)
                    delegate.onCompleteImageLoad(self);
            }

            @Override
            public void onError() {
                if (delegate != null)
                    delegate.onError(self);
            }
        });
    }

    @Override
    protected void onCompleteImageLoad(GlideDrawable drawable) {
        Bitmap bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
        float scale = bitmap.getWidth() / getWidth();

        scaleImageView.get().setImage(ImageSource.cachedBitmap(bitmap));

        if (isScaleAspectFill()) {
            scaleImageView.get().setScaleAndCenter(scale, new PointF(bitmap.getWidth() / 2, bitmap.getHeight() / 2));
            scaleImageView.get().setMinScale(scale);
        }

        progressBar.setVisibility(GONE);
        updateScaleEnabled();
    }

    @Override
    protected void onStartImageLoad() {
        scaleImageView.get().recycle();
        scaleImageView.get().resetScaleAndCenter();
        progressBar.setVisibility(VISIBLE);

        super.onStartImageLoad();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void reset() {
        scaleImageView.get().resetScaleAndCenter();
    }

    public void setScaleEnabled(boolean scaleEnabled) {
        if (scaleEnabled == this.scaleEnabled)
            return;

        this.scaleEnabled = scaleEnabled;

        updateScaleEnabled();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void updateScaleEnabled() {
        scaleImageView.get().setPanEnabled(scaleEnabled);
        scaleImageView.get().setZoomEnabled(scaleEnabled);
    }
}
