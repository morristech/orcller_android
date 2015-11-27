package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.ImageMedia;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.album.VideoMedia;

import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/24/15.
 */
public class MediaContainerView extends PSFrameLayout implements MediaContainer {
    private boolean controlEnabled = true;
    private boolean controlEnabledChanged;
    private int imageLoadType = MediaView.ImageLoadType.Thumbnail.getValue();
    private Media model;
    private MediaView mediaView;

    public MediaContainerView(Context context) {
        super(context);
    }

    public MediaContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MediaContainerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void commitProperties() {
        if (controlEnabledChanged) {
            controlEnabledChanged = false;

            if (getVideoMediaView() != null)
                getVideoMediaView().setControlEnabled(controlEnabled);
        }
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.MediaContainerView, defStyleAttr, defStyleRes);
        controlEnabled = typedArray.getBoolean(R.styleable.MediaContainerView_controlEnabled, true);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isControlEnabled() {
        return controlEnabled;
    }

    public void setControlEnabled(boolean controlEnabled) {
        if (controlEnabled == this.controlEnabled)
            return;

        this.controlEnabled = controlEnabled;
        controlEnabledChanged = true;

        invalidateProperties();
    }

    public int getImageLoadType() {
        return imageLoadType;
    }

    public void setImageLoadType(int imageLoadType) {
        this.imageLoadType = imageLoadType;
    }

    public ImageView getImageView() {
        return null;//mediaView.getImageView();
    }

    public MediaView getMediaView() {
        return mediaView;
    }

    public VideoMediaView getVideoMediaView() {
        if (mediaView instanceof VideoMediaView)
            return (VideoMediaView) mediaView;
        return null;
    }

    public Media getModel() {
        return model;
    }

    public void setModel(Media model) {
        if (ObjectUtils.equals(this.model, model))
            return;

        this.model = model;
        modelChanged();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private MediaView createMediaView(Media model) {
        if (model instanceof ImageMedia)
            return new ImageMediaView(getContext());
        if (model instanceof VideoMedia)
            return new VideoMediaView(getContext());
        return null;
    }

    private Media getMediaModel() {
        if (mediaView != null)
            mediaView.getModel();
        return null;
    }

    private void modelChanged() {
        Media media = getMediaModel();

        if (mediaView == null || media == null || model.type != media.type) {
            if (mediaView != null)
                removeView(mediaView);

            mediaView = createMediaView(model);

            if (mediaView != null) {
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
                addView(mediaView, 0, params);
            }
        }

        if (mediaView != null) {
            if (getVideoMediaView() != null)
                getVideoMediaView().setControlEnabled(controlEnabled);

            mediaView.setImageLoadType(imageLoadType);
            mediaView.setModel(model);
        }
    }
}
