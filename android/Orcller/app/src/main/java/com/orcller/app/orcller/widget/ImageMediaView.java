package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.orcller.app.orcller.R;

/**
 * Created by pisces on 11/16/15.
 */
public class ImageMediaView extends MediaView {

    public ImageMediaView(Context context) {
        super(context);
    }

    public ImageMediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: MediaView
    // ================================================================================================

    @Override
    protected void loadImages() {
        final MediaView self = this;

        loadImages(new CompleteHandler() {
            @Override
            public void onComplete() {
                if (delegate != null)
                    delegate.onCompleteImageLoad(self);
            }

            @Override
            public void onError() {
                if (delegate != null)
                    delegate.onError(self);
            }
        });
    }
}
