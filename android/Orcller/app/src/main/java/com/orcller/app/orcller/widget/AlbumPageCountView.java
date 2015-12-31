package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.orcller.app.orcller.R;

import pisces.psfoundation.utils.GraphicUtils;

/**
 * Created by pisces on 11/22/15.
 */
public class AlbumPageCountView extends TextView {

    public AlbumPageCountView(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public AlbumPageCountView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public AlbumPageCountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperties(context, attrs, defStyleAttr, 0);
    }

    private void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setMargins(0, 0, GraphicUtils.convertDpToPixel(5), 0);

        setLayoutParams(params);
        setShadowLayer(8, 0, 0, Color.argb(90, 0, 0, 0));
        setTextColor(Color.WHITE);
        setTextSize(28);
    }

    public void hide() {
        if (getParent() != null) {
            ViewGroup parent = (ViewGroup) getParent();
            parent.removeView(this);
        }
    }

    public void show(ViewGroup viewGroup) {
        if (getParent() == null) {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            viewGroup.addView(this, params);
        }
    }
}
