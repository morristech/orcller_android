package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.orcller.app.orcller.R;

import java.lang.ref.WeakReference;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/22/15.
 */
public class ImageMediaScrollView extends MediaView {
    private boolean scaleEnabled = true;
    private float baseScale;
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
        setImageLoadType(ImageLoadType.LowResolution.value() | ImageLoadType.StandardResoultion.value());
        imageView.setVisibility(INVISIBLE);
        addView(scaleImageView.get());
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
        loadImages(new CompleteHandler() {
            @Override
            public void onComplete() {
            }

            @Override
            public void onError() {
            }
        });
    }

    @Override
    protected void onCompleteImageLoad(GlideDrawable drawable) {
        Bitmap bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
        baseScale = (float) Math.min(getWidth(), getHeight()) / Math.min(bitmap.getWidth(), bitmap.getHeight());

        scaleImageView.get().setImage(ImageSource.cachedBitmap(bitmap));

        if (isScaleAspectFill()) {
            scaleImageView.get().setScaleAndCenter(baseScale, new PointF(bitmap.getWidth() / 2, bitmap.getHeight() / 2));
        }

        progressBar.setVisibility(GONE);
        updateScaleEnabled();
    }

    @Override
    protected void onError() {
        super.onError();

        if (!scaleImageView.get().isImageLoaded())
            imageView.setVisibility(VISIBLE);
    }

    @Override
    protected void onStartImageLoad() {
        imageView.setVisibility(INVISIBLE);
        scaleImageView.get().recycle();
        reset();

        super.onStartImageLoad();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void reset() {
        scaleImageView.get().setScaleAndCenter(baseScale, scaleImageView.get().getCenter());
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
