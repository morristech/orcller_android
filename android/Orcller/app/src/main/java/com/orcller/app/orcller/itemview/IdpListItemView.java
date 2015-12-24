package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.orcller.app.orcller.R;

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
    }

    @Override
    protected void inflateLayout(Context context) {
        inflate(context, R.layout.itemview_list_idp, this);

        imageView = (ImageView) findViewById(pisces.android.R.id.imageView);
        detailTextView = (TextView) findViewById(pisces.android.R.id.detailTextView);
        titleTextView = (TextView) findViewById(pisces.android.R.id.titleTextView);
        subtitleTextView = (TextView) findViewById(pisces.android.R.id.subtitleTextView);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void setImageDrawable(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }
}
