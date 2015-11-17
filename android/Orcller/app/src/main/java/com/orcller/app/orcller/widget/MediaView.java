package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.model.album.Media;

import java.io.File;
import java.net.URL;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.URLUtils;

/**
 * Created by pisces on 11/16/15.
 */
abstract public class MediaView extends RelativeLayout {
    public enum ImageLoadType {
        Thumbnail(1<<0),
        LowResolution(1<<1),
        StandardResoultion(1<<2);

        private int value;

        private ImageLoadType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private int imageLoadType;
    private Drawable placeholder;
    private ImageView emptyImageView;
    private Media media;
    protected ImageView imageView;
    protected MediaViewDelegate delegate;

    public MediaView(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public MediaView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public MediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public MediaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public int getImageLoadType() {
        return imageLoadType;
    }

    public void setImageLoadType(int imageLoadType) {
        this.imageLoadType = imageLoadType;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        if (Model.equasl(this.media, media))
            return;

        EventBus.getDefault().unregister(this);

        this.media = media;

        if (this.media != null)
            EventBus.getDefault().register(this, MediaManager.DidChangeImages.class);

        modelChanged();
    }

    public Drawable getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(Drawable placeholder) {
        this.placeholder = placeholder;
    }

    public void setDelegate(MediaViewDelegate delegate) {
        this.delegate = delegate;
    }

    public void onEventMainThread(Object event) {

    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    /**
     * @abstract
     */
    abstract protected void loadImages();

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        emptyImageView = new ImageView(this.getContext());
        imageView = new ImageView(this.getContext());
        TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.MediaView, defStyleAttr, defStyleRes);

        setImageLoadType(typedArray.getInt(R.styleable.MediaView_imageLoadType, ImageLoadType.Thumbnail.getValue()));
        setPlaceholder(typedArray.getDrawable(R.styleable.MediaView_placeholder));
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        emptyImageView.setVisibility(GONE);
        addView(imageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(emptyImageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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

    // ================================================================================================
    //  Private
    // ================================================================================================

    private boolean canLoadLowResolution() {
        return (imageLoadType & ImageLoadType.LowResolution.getValue()) == ImageLoadType.LowResolution.getValue();
    }

    private boolean canLoadStandardResolution() {
        return (imageLoadType & ImageLoadType.StandardResoultion.getValue()) == ImageLoadType.StandardResoultion.getValue();
    }

    private boolean canLoadThumbnail() {
        return (imageLoadType & ImageLoadType.Thumbnail.getValue()) == ImageLoadType.Thumbnail.getValue();
    }

    private void loadImage(String url, final CompleteHandler completeHandler) {
        if (TextUtils.isEmpty(url)) {
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
            Object source = URLUtils.isLocal(url) ? new File(url) : new URL(SharedObject.toFullMediaUrl(url));

            Glide.with(getContext())
                    .load(source)
                    .placeholder(placeholder)
                    .dontAnimate()
                    .listener(new RequestListener<Object, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                            handler.onError();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            imageView.setImageDrawable(resource);

                            if (resource != null)
                                handler.onComplete();
                            else
                                handler.onError();
                            return false;
                        }
                    })
                    .into(imageView);
        } catch (Exception e) {
            handler.onError();
        }
    }

    private void loadLowResolution(final CompleteHandler completeHandler) {
        if (media.images.low_resolution.isEmpty()) {
            if (completeHandler != null)
                completeHandler.onError();
            return;
        }

        loadImage(media.images.low_resolution.url, new CompleteHandler() {
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
        if (media.images.standard_resolution.isEmpty()) {
            if (completeHandler != null)
                completeHandler.onError();
            return;
        }

        loadImage(media.images.standard_resolution.url, new CompleteHandler() {
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
        if (media.images.thumbnail.isEmpty()) {
            if (completeHandler != null)
                completeHandler.onError();
            return;
        }

        loadImage(media.images.thumbnail.url, new CompleteHandler() {
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
        emptyImageView.setVisibility(GONE);
        loadImages();
    }

    public interface CompleteHandler {
        void onComplete();
        void onError();
    }

    public interface MediaViewDelegate {
        void onComplete(Drawable image);
        void onError();
    }
}
