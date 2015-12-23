package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.orcller.app.orcller.common.SharedObject;

import pisces.psuikit.itemview.ListSwitchItemView;

/**
 * Created by pisces on 12/23/15.
 */
public class ApplicationOptionsItemView extends ListSwitchItemView implements CompoundButton.OnCheckedChangeListener {

    public ApplicationOptionsItemView(Context context) {
        super(context);
    }

    public ApplicationOptionsItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ApplicationOptionsItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: ListBaseItemView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        getSwitch().setChecked(SharedObject.get().isAllowsAutoSlide());
        getSwitch().setOnCheckedChangeListener(this);
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedObject.get().setAllowsAutoSlide(isChecked);
    }
}
