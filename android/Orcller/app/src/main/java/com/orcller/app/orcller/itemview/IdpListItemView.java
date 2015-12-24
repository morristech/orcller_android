package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.itemview.ListBaseItemView;

/**
 * Created by pisces on 12/24/15.
 */
public class IdpListItemView extends ListBaseItemView {
    private ImageView imageView;

    public IdpListItemView(Context context) {
        super(context);
    }

    public IdpListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IdpListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: ListBaseItemView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        imageView = new ImageView(context);
        FrameLayout container = new FrameLayout(context);
        ViewGroup viewGroup = (ViewGroup) detailTextView.getParent();

        LayoutParams containerParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;

        container.setPadding(0, 0, GraphicUtils.convertDpToPixel(8), 0);
        container.addView(imageView, new LayoutParams(GraphicUtils.convertDpToPixel(32), GraphicUtils.convertDpToPixel(32)));
        viewGroup.addView(container, 0, containerParams);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void setImageDrawable(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }
}
