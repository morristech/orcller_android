package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by pisces on 12/11/15.
 */
public class AlbumDataGridItemView extends AbstractDataGridItemView {

    public AlbumDataGridItemView(Context context) {
        super(context);
    }

    public AlbumDataGridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumDataGridItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, getLayoutRes(), this);
    }

    @Override
    protected int getLayoutRes() {
        return 0;
    }

    @Override
    protected void modelChanged() {

    }
}
