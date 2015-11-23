package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.orcller.app.orcller.model.album.ImageMedia;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.album.VideoMedia;

import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/23/15.
 */
public class MediaScrollView extends PSFrameLayout {
    private boolean modelChanged;
    private Media model;
    private PSFrameLayout mediaView;

    public MediaScrollView(Context context) {
        super(context);
    }

    public MediaScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MediaScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mediaView instanceof VideoMediaView) {
            ViewGroup.LayoutParams params = mediaView.getLayoutParams();
            params.height = getMeasuredWidth();
            mediaView.setLayoutParams(params);
        }
    }

    @Override
    protected void setUpSubviews(Context context) {
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public ImageView getImageView() {
        if (mediaView instanceof ImageMediaScrollView)
            return ((ImageMediaScrollView) mediaView).getMediaView().getImageView();
        if (mediaView instanceof VideoMediaView)
            return ((VideoMediaView) mediaView).getImageView();
        return null;
    }

    public Media getModel() {
        return model;
    }

    public void setModel(Media model) {
        if (Model.equasl(this.model, model))
            return;

        this.model = model;
        modelChanged = true;

        invalidateProperties();
    }

    public ImageMediaScrollView getImageMediaScrollView() {
        if (mediaView instanceof ImageMediaScrollView)
            return (ImageMediaScrollView) mediaView;
        return null;
    }

    public VideoMediaView getVideoMediaView() {
        if (mediaView instanceof VideoMediaView)
            return (VideoMediaView) mediaView;
        return null;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private PSFrameLayout createMediaView(Media model) {
        if (model instanceof ImageMedia) {
            ImageMediaScrollView view = new ImageMediaScrollView(getContext());
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
        if (mediaView instanceof ImageMediaScrollView)
            return ((ImageMediaScrollView) mediaView).getModel();
        if (mediaView instanceof VideoMediaView)
            return ((VideoMediaView) mediaView).getModel();
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

        if (mediaView instanceof ImageMediaScrollView)
            ((ImageMediaScrollView) mediaView).setModel((ImageMedia) model);
        else if (mediaView instanceof  VideoMediaView)
            ((VideoMediaView) mediaView).setModel(model);
    }
}
