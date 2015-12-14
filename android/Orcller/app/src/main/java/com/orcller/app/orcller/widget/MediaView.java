package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.event.MediaEvent;
import com.orcller.app.orcller.model.album.Image;
import com.orcller.app.orcller.model.album.Media;

import java.io.File;
import java.net.URL;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psfoundation.utils.URLUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/16/15.
 */
abstract public class MediaView extends PSFrameLayout implements View.OnClickListener {
    public enum ImageLoadType {
        Thumbnail(1<<0),
        LowResolution(1<<1),
        StandardResoultion(1<<2);

        private int value;

        private ImageLoadType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    private boolean clickEnabled;
    private boolean scaleAspectFill = true;
    private int imageLoadType;
    protected Point imageSize;
    private Drawable placeholder;
    private Media model;
    protected Delegate delegate;
    private ImageView emptyImageView;
    protected ImageView imageView;
    protected ProgressBar progressBar;

    public MediaView(Context context) {
        super(context);
    }

    public MediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_media, this);

        imageView = (ImageView) findViewById(R.id.imageView);
        emptyImageView = (ImageView) findViewById(R.id.emptyImageView);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MediaView, defStyleAttr, defStyleRes);
        try {
            setImageLoadType(ta.getInt(R.styleable.MediaView_imageLoadType, ImageLoadType.Thumbnail.value()));
            setPlaceholder(ta.getDrawable(R.styleable.MediaView_placeholder));
        } finally {
            ta.recycle();
        }

        setProgressBar(new ProgressBar(context, null, android.R.attr.progressBarStyleSmall));
    }

    @Override
    public void onClick(View v) {
        if (delegate != null)
            delegate.onClick(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isClickEnabled() {
        return clickEnabled;
    }

    public void setClickEnabled(boolean clickEnabled) {
        if (clickEnabled == this.clickEnabled)
            return;

        this.clickEnabled = true;

        clickEnabled();
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public int getImageLoadType() {
        return imageLoadType;
    }

    public void setImageLoadType(int imageLoadType) {
        this.imageLoadType = imageLoadType;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public Media getModel() {
        return model;
    }

    public void setModel(Media model) {
        if (ObjectUtils.equals(this.model, model))
            return;

        EventBus.getDefault().unregister(this);

        this.model = model;

        if (this.model != null)
            EventBus.getDefault().register(this);

        modelChanged();
    }

    public Drawable getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(Drawable placeholder) {
        this.placeholder = placeholder;
    }

    public void setProgressBar(ProgressBar progressBar) {
        if (this.progressBar != null) {
            removeView(this.progressBar);
            this.progressBar = null;
        }

        this.progressBar = progressBar;

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        progressBar.setVisibility(GONE);
        addView(progressBar, params);
    }

    public boolean isScaleAspectFill() {
        return scaleAspectFill;
    }

    public void setScaleAspectFill(boolean scaleAspectFill) {
        if (scaleAspectFill == this.scaleAspectFill)
            return;;

        this.scaleAspectFill = scaleAspectFill;

        if (scaleAspectFill)
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        else
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (model != null && event instanceof MediaEvent) {
            Media media = (Media) ((MediaEvent) event).getObject();
            if (media.id == model.id) {
                model.images = media.images;
                modelChanged();
            }
        }
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    /**
     * @abstract
     */
    abstract protected void loadImages();

    protected void clickEnabled() {
        if (clickEnabled)
            setOnClickListener(this);
        else
            setOnClickListener(null);
    }

    protected void loadImages(CompleteHandler completeHandler) {
        if (canLoadThumbnail()) {
            loadThumnail(completeHandler);
        } else if (canLoadLowResolution()) {
            loadLowResolution(completeHandler);
        } else if (canLoadStandardResolution()) {
            loadStandardResolution(completeHandler);
        }
    }

    protected void onCompleteImageLoad(GlideDrawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    protected void onError() {
    }

    protected void onStartImageLoad() {
        Glide.clear(imageView);

        if (delegate != null)
            delegate.onStartImageLoad(this);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private boolean canLoadLowResolution() {
        return (imageLoadType & ImageLoadType.LowResolution.value()) == ImageLoadType.LowResolution.value();
    }

    private boolean canLoadStandardResolution() {
        return (imageLoadType & ImageLoadType.StandardResoultion.value()) == ImageLoadType.StandardResoultion.value();
    }

    private boolean canLoadThumbnail() {
        return (imageLoadType & ImageLoadType.Thumbnail.value()) == ImageLoadType.Thumbnail.value();
    }

    private void loadImage(Image image, final CompleteHandler completeHandler) {
        if (TextUtils.isEmpty(image.url)) {
            completeHandler.onError();
            return;
        }

        final CompleteHandler handler = new CompleteHandler() {
            @Override
            public void onError() {
                emptyImageView.setVisibility(VISIBLE);

                if (completeHandler != null)
                    completeHandler.onError();
            }

            @Override
            public void onComplete() {
                emptyImageView.setVisibility(GONE);

                if (completeHandler != null)
                    completeHandler.onComplete();
            }
        };

        try {
            Object source = URLUtils.isLocal(image.url) ? new File(image.url) : new URL(SharedObject.toFullMediaUrl(image.url));
            Glide.with(getContext())
                    .load(source)
                    .placeholder(placeholder)
                    .dontAnimate()
                    .override(imageSize.x, imageSize.y)
                    .listener(new RequestListener<Object, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                            if (BuildConfig.DEBUG)
                                Log.e("onException", e, model);

                            onError();
                            handler.onError();
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            onCompleteImageLoad(resource);

                            if (resource != null)
                                handler.onComplete();
                            else
                                handler.onError();
                            return true;
                        }
                    })
                    .into(imageView);
        } catch (Exception e) {
            handler.onError();
        }
    }

    private void loadLowResolution(final CompleteHandler completeHandler) {
        if (model.images.low_resolution.isEmpty()) {
            if (completeHandler != null)
                completeHandler.onError();
            return;
        }

        loadImage(model.images.low_resolution, new CompleteHandler() {
            @Override
            public void onError() {
                if (completeHandler != null)
                    completeHandler.onError();
            }

            @Override
            public void onComplete() {
                if (completeHandler != null)
                    completeHandler.onComplete();

                if (canLoadStandardResolution())
                    loadStandardResolution(completeHandler);
            }
        });
    }

    private void loadStandardResolution(final CompleteHandler completeHandler) {
        if (model.images.standard_resolution.isEmpty()) {
            if (completeHandler != null)
                completeHandler.onError();
            return;
        }

        loadImage(model.images.standard_resolution, new CompleteHandler() {
            @Override
            public void onError() {
                if (completeHandler != null)
                    completeHandler.onError();
            }

            @Override
            public void onComplete() {
                if (completeHandler != null)
                    completeHandler.onComplete();
            }
        });
    }

    private void loadThumnail(final CompleteHandler completeHandler) {
        if (model.images.thumbnail.isEmpty()) {
            if (completeHandler != null)
                completeHandler.onError();
            return;
        }

        loadImage(model.images.thumbnail, new CompleteHandler() {
            @Override
            public void onError() {
                if (completeHandler != null)
                    completeHandler.onError();
            }

            @Override
            public void onComplete() {
                if (completeHandler != null)
                    completeHandler.onComplete();

                if (canLoadLowResolution()) {
                    loadLowResolution(completeHandler);
                } else if (canLoadStandardResolution()) {
                    loadStandardResolution(completeHandler);
                }
            }
        });
    }

    private void modelChanged() {
        float rate = (float) Math.min(Application.getWindowWidth(), Application.getWindowHeight()) / Math.min(model.images.thumbnail.width, model.images.thumbnail.height);
        int w = Math.round(model.images.thumbnail.width * rate);
        int h = Math.round(model.images.thumbnail.width * rate);
        imageSize = new Point(w, h);

        emptyImageView.setVisibility(GONE);
        onStartImageLoad();
        loadImages();
    }

    public interface CompleteHandler {
        void onComplete();
        void onError();
    }

    public interface Delegate {
        void onClick(MediaView view);
        void onCompleteImageLoad(MediaView view);
        void onError(MediaView view);
        void onStartImageLoad(MediaView view);
    }
}
