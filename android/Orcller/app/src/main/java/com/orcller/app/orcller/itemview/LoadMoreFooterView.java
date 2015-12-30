package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ProgressBar;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 12/30/15.
 */
public class LoadMoreFooterView extends PSFrameLayout {
    private ProgressBar progressBar;

    public LoadMoreFooterView(Context context) {
        super(context);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;

        addView(progressBar, params);
        setBackgroundResource(android.R.color.transparent);
        setMinimumHeight(GraphicUtils.convertDpToPixel(30));
    }
}
