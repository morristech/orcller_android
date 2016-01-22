package pisces.psuikit.ext;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import pisces.android.R;
import pisces.psfoundation.utils.DataLoadValidator;

/**
 * Created by pisces on 12/11/15.
 */
public class PSViewPager extends ViewPager implements PSComponent, DataLoadValidator.Client {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    private boolean pagingEnabled = true;
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;

    public PSViewPager(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public PSViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    // ================================================================================================
    //  Overridden: ViewPager
    // ================================================================================================

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!initializedSubviews) {
            initializedSubviews = true;
            setUpSubviews(getContext());
        }

        invalidateProperties();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            boolean wrapHeight = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST;

            if (wrapHeight) {
                int width = getMeasuredWidth(), height = getMeasuredHeight();
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

                if(getChildCount() > 0) {
                    View firstChild = getChildAt(0);
                    firstChild.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
                    height = firstChild.getMeasuredHeight();
                }

                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            if (!pagingEnabled)
                return false;
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (pagingEnabled)
                        return super.onTouchEvent(ev);
                    return pagingEnabled;
                default:
                    return super.onTouchEvent(ev);
            }
        } catch (Exception e) {
            return super.onTouchEvent(ev);
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isPagingEnabled() {
        return pagingEnabled;
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        this.pagingEnabled = pagingEnabled;
    }

    public boolean isImmediatelyUpdating() {
        return immediatelyUpdating;
    }

    public void setImmediatelyUpdating(boolean immediatelyUpdating) {
        this.immediatelyUpdating = immediatelyUpdating;
    }

    public void invalidateProperties() {
        if (initializedSubviews || immediatelyUpdating)
            commitProperties();
    }

    public void validateProperties() {
        commitProperties();
    }

    public void endDataLoading() {
        dataLoadValidator.endDataLoading();
    }

    public boolean isFirstLoading() {
        return dataLoadValidator.isFirstLoading();
    }

    public boolean invalidDataLoading() {
        return dataLoadValidator.invalidDataLoading();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected void commitProperties() {
    }

    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PSViewPager, defStyleAttr, defStyleRes);
        try {
            setPagingEnabled(ta.getBoolean(R.styleable.PSViewPager_pagingEnabled, true));
        } finally {
            ta.recycle();
        }
    }

    protected void setUpSubviews(Context context) {
    }
}
