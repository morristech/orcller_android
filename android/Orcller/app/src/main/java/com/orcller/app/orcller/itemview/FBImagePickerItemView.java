package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.caches.FBPhotoCaches;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.facebook.FBAlbum;
import com.orcller.app.orcllermodules.model.facebook.FBPhoto;
import com.orcller.app.orcllermodules.model.facebook.FBPhotoImage;
import com.orcller.app.orcllermodules.model.facebook.FBVideoAlbum;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;

import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 11/26/15.
 */
public class FBImagePickerItemView extends PSLinearLayout {
    private ImageView imageView;
    private TextView nameTextView;
    private TextView countTextView;
    private FBAlbum model;

    public FBImagePickerItemView(Context context) {
        super(context);
    }

    public FBImagePickerItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FBImagePickerItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FBImagePickerItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void commitProperties() {
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_fb_imagepicker, this);

        imageView = (ImageView) findViewById(R.id.imageView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        countTextView = (TextView) findViewById(R.id.countTextView);
    }

    @Override
    protected void setUpSubviews(Context context) {
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public FBAlbum getModel() {
        return model;
    }

    public void setModel(FBAlbum model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void loadImage(final FBPhotoImage image) {
        Glide.with(getContext())
                .load(image.source)
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
        nameTextView.setText(model.name);
        countTextView.setText(String.valueOf(model.count));
        countTextView.setVisibility(model.count > 0 ? VISIBLE : GONE);

        Glide.clear(imageView);
        imageView.setImageDrawable(null);

        if (model instanceof FBVideoAlbum) {
            imageView.setImageResource(R.drawable.img_fb_videos);
        } else if (model.cover_photo == null) {
            imageView.setImageResource(R.drawable.img_fb_empty_album);
        } else {
            FBPhotoCaches.getDefault().getPhoto(
                    model.cover_photo.id, new FBSDKRequest.CompleteHandler<FBPhoto>() {
                        @Override
                        public void onComplete(FBPhoto result, APIError error) {
                            if (error == null)
                                loadImage(result.images.get(3));
                        }
                    }
            );
        }
    }
}
