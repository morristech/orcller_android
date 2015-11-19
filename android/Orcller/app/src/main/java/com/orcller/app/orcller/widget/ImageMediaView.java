package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

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

    public ImageMediaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
                    delegate.onCompleteImageLoad(self, imageView.getDrawable());
            }

            @Override
            public void onError() {
                if (delegate != null)
                    delegate.onError(self);
            }
        });
    }
}
