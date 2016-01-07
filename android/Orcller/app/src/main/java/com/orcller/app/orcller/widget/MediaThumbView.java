package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.Media;

import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 1/7/16.
 */
public class MediaThumbView extends PSFrameLayout {
    private boolean modelChanged;
    private ImageView imageView;
    private ImageView videoIcon;
    private View errorView;
    private ProgressBar progressBar;
    private Media model;

    public MediaThumbView(Context context) {
        super(context);
    }

    public MediaThumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaThumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        inflate(context, R.layout.view_media_thumb, this);

        imageView = (ImageView) findViewById(R.id.imageView);
        videoIcon = (ImageView) findViewById(R.id.videoIcon);
        errorView = findViewById(R.id.errorView);
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

        progressBar.setVisibility(GONE);
        addView(progressBar, params);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Media getModel() {
        return model;
    }

    public void setModel(Media model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;
        modelChanged = true;

        invalidateProperties();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void modelChanged() {
        Glide.clear(imageView);
        errorView.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        videoIcon.setVisibility(model.isVideo() ? VISIBLE : GONE);

        Glide.with(getContext())
                .load(SharedObject.toFullMediaUrl(model.images.low_resolution.url))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        errorView.setVisibility(VISIBLE);
                        progressBar.setVisibility(GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(GONE);
                        return false;
                    }
                })
                .into(imageView);
    }
}
