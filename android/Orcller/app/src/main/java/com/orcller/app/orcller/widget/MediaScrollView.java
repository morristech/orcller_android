package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ProgressBar;

import com.orcller.app.orcller.model.ImageMedia;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.model.VideoMedia;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
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
    private MediaView.Delegate mediaViewDelegate;

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
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (getVideoMediaView() != null)
            getVideoMediaView().setEnabled(enabled);
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

    public MediaView.Delegate getMediaViewDelegate() {
        return mediaViewDelegate;
    }

    public void setMediaViewDelegate(MediaView.Delegate mediaViewDelegate) {
        this.mediaViewDelegate = mediaViewDelegate;
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

        if (getImageMediaScrollView() != null) {
            if (!scaleEnabled)
                getImageMediaScrollView().reset();

            getImageMediaScrollView().setScaleEnabled(scaleEnabled);
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private MediaView createMediaView(Media model) {
        if (model instanceof ImageMedia) {
            ImageMediaScrollView view = new ImageMediaScrollView(getContext());
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;

            view.setLayoutParams(params);
            view.setScaleAspectFill(scaleAspectFill);
            view.setScaleEnabled(scaleEnabled);
            view.setProgressBar(new ProgressBar(getContext(), null, android.R.attr.progressBarStyle));

            return view;
        }
        if (model instanceof VideoMedia) {
            VideoMediaView view = new VideoMediaView(getContext());
            VideoMedia media = (VideoMedia) model;
            float scale = (float) Application.getWindowWidth() / media.videos.standard_resolution.width;
            int w = Math.round(media.videos.standard_resolution.width * scale);
            int h = Math.round(media.videos.standard_resolution.height * scale);
            LayoutParams params = new LayoutParams(w, h);
            params.gravity = Gravity.CENTER;

            view.setImageLoadType(MediaView.ImageLoadType.LowResolution.value() | MediaView.ImageLoadType.StandardResoultion.value());
            view.setLayoutParams(params);
            view.setProgressBar(new ProgressBar(getContext(), null, android.R.attr.progressBarStyle));

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
            if (mediaView != null) {
                mediaView.setDelegate(null);
                removeView(mediaView);
            }

            mediaView = createMediaView(model);

            if (mediaView != null) {
                mediaView.setClickEnabled(true);
                mediaView.setDelegate(mediaViewDelegate);
                addView(mediaView, 0);
            }
        }

        if (mediaView != null)
            mediaView.setModel(model);
    }
}
