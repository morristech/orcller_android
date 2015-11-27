package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcller.app.orcller.R;

import pisces.instagram.sdk.model.ApiInstagram;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSImageView;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 11/27/15.
 */
public class IGImagePickerItemView extends PSLinearLayout {
    private boolean allowsShowBackground = true;
    private boolean allowsShowBackgroundChanged;
    private PSImageView imageView;
    private TextView idTextView;
    private TextView nameTextView;
    private ApiInstagram.User model;

    public IGImagePickerItemView(Context context) {
        super(context);
    }

    public IGImagePickerItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IGImagePickerItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void commitProperties() {
        if (allowsShowBackgroundChanged) {
            allowsShowBackgroundChanged = false;
            setBackgroundResource(allowsShowBackground ? R.drawable.itemview_profile_background : 0);
        }
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_instagram_imagepicker, this);

        imageView = (PSImageView) findViewById(R.id.imageView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        idTextView = (TextView) findViewById(R.id.idTextView);

        setBackgroundResource(R.drawable.itemview_profile_background);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isAllowsShowBackground() {
        return allowsShowBackground;
    }

    public void setAllowsShowBackground(boolean allowsShowBackground) {
        if (allowsShowBackground == this.allowsShowBackground)
            return;

        this.allowsShowBackground = allowsShowBackground;
        allowsShowBackgroundChanged = true;

        invalidateProperties();
    }

    public ApiInstagram.User getModel() {
        return model;
    }

    public void setModel(ApiInstagram.User model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void loadImage() {
        Glide.with(getContext())
                .load(model.profile_picture)
                .dontAnimate()
                .listener(new RequestListener<Object, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        imageView.setImageDrawable(resource);
                        return true;
                    }
                })
                .into(imageView);
    }

    private void modelChanged() {
        nameTextView.setText(model.username);
        idTextView.setText(String.valueOf(model.full_name));
        idTextView.setVisibility(TextUtils.isEmpty(model.full_name) ? GONE : VISIBLE);

        Glide.clear(imageView);
        imageView.setImageDrawable(null);
        loadImage();
    }
}
