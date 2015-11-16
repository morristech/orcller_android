package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.orcller.app.orcller.R;

/**
 * Created by pisces on 11/16/15.
 */
public class ImageMediaView extends MediaView {
    @Override
    protected void loadImages() {
        loadImages(new CompleteHandler() {
            @Override
            public void onComplete() {
                if (delegate != null)
                    delegate.onComplete(imageView.getDrawable());
            }

            @Override
            public void onError() {
                if (delegate != null)
                    delegate.onError();
            }
        });
    }

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
}
