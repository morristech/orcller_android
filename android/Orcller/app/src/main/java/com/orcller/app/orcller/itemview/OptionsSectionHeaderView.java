package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;

import com.orcller.app.orcller.R;

import pisces.psuikit.itemview.ListBaseHeaderView;

/**
 * Created by pisces on 12/23/15.
 */
public class OptionsSectionHeaderView extends ListBaseHeaderView {
    public OptionsSectionHeaderView(Context context) {
        super(context);
    }

    public OptionsSectionHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptionsSectionHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: ListBaseHeaderView
    // ================================================================================================

    @Override
    protected int getLayoutResId() {
        return R.layout.headerview_options_section;
    }
}
