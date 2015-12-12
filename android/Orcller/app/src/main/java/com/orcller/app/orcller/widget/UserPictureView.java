package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;
import com.orcller.app.orcllermodules.model.BaseUser;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFrameLayout;
import pisces.psuikit.ext.PSImageView;

/**
 * Created by pisces on 11/28/15.
 */
public class UserPictureView extends PSFrameLayout implements View.OnClickListener {
    private boolean allowsProfileOpen;
    private BaseUser model;
    private SharedObject.SizeType sizeType = SharedObject.SizeType.Small;
    private PSImageView imageView;
    private View overlapView;

    public UserPictureView(Context context) {
        super(context);
    }

    public UserPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserPictureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // ================================================================================================
    //  Overridden: PSImageView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.UserPictureView, defStyleAttr, defStyleRes);
        try {
            setAllowsProfileOpen(ta.getBoolean(R.styleable.UserPictureView_allowsProfileOpen, true));
        } finally {
            ta.recycle();
        }

        imageView = new PSImageView(context);
        overlapView = new View(context);

        imageView.setBorderColor(getResources().getColor(R.color.border_profile_imageview));
        imageView.setBorderWidth(GraphicUtils.convertDpToPixel(1));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        overlapView.setBackgroundResource(R.drawable.background_ripple_transparent);
        addView(imageView);
        addView(overlapView);
        setClickable(true);
        setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        imageView.setCornerRadius(getMeasuredHeight() / 2);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isAllowsProfileOpen() {
        return allowsProfileOpen;
    }

    public void setAllowsProfileOpen(boolean allowsProfileOpen) {
        this.allowsProfileOpen = allowsProfileOpen;
    }

    public BaseUser getModel() {
        return model;
    }

    public void setModel(BaseUser model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    public SharedObject.SizeType getSizeType() {
        return sizeType;
    }

    public void setSizeType(SharedObject.SizeType sizeType) {
        this.sizeType = sizeType;
    }

    public void reload() {
        modelChanged();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onClick(View v) {
        if (allowsProfileOpen) {
            String link = CustomSchemeGenerator.createUserProfile(model).toString();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Application.applicationContext().startActivity(intent);
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private int getPlaceholderImage() {
        if (SharedObject.SizeType.Small.equals(sizeType))
            return R.drawable.profile_noimage36;
        if (SharedObject.SizeType.Medium.equals(sizeType))
            return R.drawable.profile_noimage54;
        if (SharedObject.SizeType.Large.equals(sizeType))
            return R.drawable.profile_noimage320;
        return 0;
    }

    private void modelChanged() {
        Glide.clear(imageView);
        Glide.with(getContext())
                .load(SharedObject.toUserPictureUrl(model.user_picture, sizeType))
                .placeholder(getPlaceholderImage())
                .dontAnimate()
                .listener(new RequestListener<Object, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                        imageView.setImageResource(getPlaceholderImage());
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
}
