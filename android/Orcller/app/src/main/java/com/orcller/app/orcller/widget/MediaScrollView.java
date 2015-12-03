package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;

import com.orcller.app.orcller.model.album.ImageMedia;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.album.VideoMedia;

import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/23/15.
 */
public class MediaScrollView extends PSFrameLayout implements MediaContainer {
    private boolean scaleAspectFill = false;
    private boolean scaleEnabled = true;
    private Media model;
    private MediaView mediaView;

    public MediaScrollView(Context context) {
        super(context);
    }

    public MediaScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mediaView instanceof VideoMediaView) {
            mediaView.getLayoutParams().height = getMeasuredWidth();
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public ImageMediaScrollView getImageMediaScrollView() {
        if (mediaView instanceof ImageMediaScrollView)
            return (ImageMediaScrollView) mediaView;
        return null;
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

    public boolean isScaleAspectFill() {
        return scaleAspectFill;
    }

    public void setScaleAspectFill(boolean scaleAspectFill) {
        if (scaleAspectFill == this.scaleAspectFill)
            return;

        this.scaleAspectFill = scaleAspectFill;

        if (getImageMediaScrollView() != null)
            getImageMediaScrollView().setScaleAspectFill(scaleAspectFill);
    }

    public boolean isScaleEnabled() {
        return scaleEnabled;
    }

    public void setScaleEnabled(boolean scaleEnabled) {
        if (scaleEnabled == this.scaleEnabled)
            return;

        this.scaleEnabled = scaleEnabled;

        if (getImageMediaScrollView() != null)
            getImageMediaScrollView().setScaleEnabled(scaleEnabled);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private MediaView createMediaView(Media model) {
        if (model instanceof ImageMedia) {
            ImageMediaScrollView view = new ImageMediaScrollView(getContext());
            view.setScaleAspectFill(scaleAspectFill);
            view.setScaleEnabled(scaleEnabled);
            return view;
        }
        if (model instanceof VideoMedia) {
            VideoMediaView view = new VideoMediaView(getContext());
            view.setImageLoadType(MediaView.ImageLoadType.LowResolution.getValue() | MediaView.ImageLoadType.StandardResoultion.getValue());
            return view;
        }
        return null;
    }

    private Media getMediaModel() {
        if (mediaView != null)
            return mediaView.getModel();
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

        if (mediaView != null)
            mediaView.setModel(model);
    }
}
