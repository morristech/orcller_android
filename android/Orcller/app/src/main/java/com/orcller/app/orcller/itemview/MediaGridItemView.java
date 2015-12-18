package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.Media;

/**
 * Created by pisces on 12/11/15.
 */
public class MediaGridItemView extends AbstractDataGridItemView {
    private ImageView imageView;
    private ImageView videoIcon;

    public MediaGridItemView(Context context) {
        super(context);
    }

    public MediaGridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaGridItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: AbstractDataGridItemView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, getLayoutRes(), this);

        imageView = (ImageView) findViewById(R.id.imageView);
        videoIcon = (ImageView) findViewById(R.id.videoIcon);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.itemview_mediagrid;
    }

    @Override
    protected void modelChanged() {
        Glide.clear(imageView);
        imageView.setImageDrawable(null);
        videoIcon.setVisibility(media().isVideo() ? VISIBLE : GONE);

        Glide.with(getContext())
                .load(SharedObject.toFullMediaUrl(media().images.low_resolution.url))
                .into(imageView);

    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private Media media() {
        return (Media) getModel();
    }
}
