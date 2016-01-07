package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;

import com.orcller.app.orcller.R;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.widget.Badge;
import pisces.psuikit.widget.PSButton;

/**
 * Created by pisces on 12/18/15.
 */
public class TabIndicator extends PSButton {
    private Badge badge;

    public TabIndicator(Context context) {
        super(context);
    }

    public TabIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSButton
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        badge = new Badge(context);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, GraphicUtils.convertDpToPixel(18));
        params.gravity = Gravity.RIGHT;
        params.rightMargin = GraphicUtils.convertDpToPixel(14);
        params.topMargin = GraphicUtils.convertDpToPixel(8);

        badge.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
        badge.setMinimumWidth(GraphicUtils.convertDpToPixel(18));
        badge.setTextColor(context.getResources().getColor(android.R.color.white));
        badge.setTextSize(12);
        setBackground(context.getResources().getDrawable(R.drawable.background_ripple_tabbar_main));
        addView(badge, params);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Badge getBadge() {
        return badge;
    }
}
