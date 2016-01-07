package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ProgressBar;

import com.orcller.app.orcller.R;

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

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LoadMoreFooterView, defStyleAttr, defStyleRes);
        try {
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = ta.getInt(R.styleable.LoadMoreFooterView_progressBarGravity, Gravity.TOP | Gravity.CENTER_HORIZONTAL);

            addView(progressBar, params);
            setBackgroundResource(android.R.color.transparent);
            setMinimumHeight(GraphicUtils.convertDpToPixel(30));
        } finally {
            ta.recycle();
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public int getProgressBarGravity() {
        return ((LayoutParams) progressBar.getLayoutParams()).gravity;
    }

    public void setProgressBarGravity(int gravity) {
        ((LayoutParams) progressBar.getLayoutParams()).gravity = gravity;
    }
}
