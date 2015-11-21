package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.orcller.app.orcller.model.album.Page;

import pisces.psfoundation.model.Model;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/19/15.
 */
public class PageView extends PSFrameLayout implements MediaView.MediaViewDelegate {
    private boolean controlEnabled;
    private boolean controlEnabledChanged;
    private boolean modelChanged;
    private int imageLoadType;
    private Page model;
    private MediaView mediaView;
    private PageViewDelegate delegate;

    public PageView(Context context) {
        super(context);
    }

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // ================================================================================================
    //  Overridden: PSView
    // ================================================================================================

    @Override
    protected void commitProperties() {
        if (controlEnabledChanged) {
            controlEnabledChanged = false;
            VideoMediaView view = getVideoMediaView();

            if (view != null) {
                view.setControlEnabled(controlEnabled);
            }
        }

        if (modelChanged) {
            modelChanged = false;
            modelChanged();
        }
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        controlEnabled = true;
        imageLoadType = MediaView.ImageLoadType.LowResolution.getValue();
    }

    @Override
    protected void setUpSubviews(Context context) {
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isControlEnabled() {
        return controlEnabled;
    }

    public void setControlEnabled(boolean controlEnabled) {
        this.controlEnabled = controlEnabled;
        controlEnabledChanged = true;

        invalidateProperties();
    }

    public PageViewDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(PageViewDelegate delegate) {
        this.delegate = delegate;
    }

    public int getImageLoadType() {
        return imageLoadType;
    }

    public void setImageLoadType(int imageLoadType) {
        this.imageLoadType = imageLoadType;

        if (mediaView != null)
            mediaView.setImageLoadType(imageLoadType);
    }

    public MediaView getMediaView() {
        return mediaView;
    }

    public void setMediaView(MediaView mediaView) {
        this.mediaView = mediaView;
    }

    public Page getModel() {
        return model;
    }

    public void setModel(Page model) {
        if (Model.equasl(model, this.model))
            return;

        this.model = model;
        modelChanged();
    }

    public VideoMediaView getVideoMediaView() {
        if (mediaView != null && mediaView instanceof VideoMediaView)
            return (VideoMediaView) mediaView;
        return null;
    }

    public boolean hasVideoMediaView() {
        return getVideoMediaView() != null;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onCompleteImageLoad(MediaView view, Drawable image) {
        if (delegate != null)
            delegate.onCompleteImageLoad(this, image);
    }

    public void onError(MediaView view) {
        if (delegate != null)
            delegate.onError(this);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private MediaView createMediaView() {
        if (model.media.isVideo())
            return new VideoMediaView(getContext());
        return new ImageMediaView(getContext());
    }

    private void modelChanged() {
        if (model == null)
            return;

        if (mediaView == null || model.media.type != mediaView.getModel().type) {
            removeMediaView();

            mediaView = createMediaView();
            mediaView.setDelegate(this);
            addView(mediaView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        if (hasVideoMediaView())
            getVideoMediaView().setControlEnabled(controlEnabled);

        mediaView.setImageLoadType(imageLoadType);
        mediaView.setModel(model.media);
    }

    private void removeMediaView() {
        if (mediaView != null)
            removeView(mediaView);
    }

    public interface PageViewDelegate {
        void onCompleteImageLoad(PageView view, Drawable image);
        void onError(PageView view);
    }
}
