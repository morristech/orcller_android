package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Media;

import pisces.psfoundation.utils.GraphicUtils;

/**
 * Created by pisces on 12/11/15.
 */
public class AlbumCoverGridItemView extends AbstractDataGridItemView {
    private ImageView imageView;
    private ImageView videoIcon;
    private TextView textView;

    public AlbumCoverGridItemView(Context context) {
        super(context);
    }

    public AlbumCoverGridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumCoverGridItemView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        textView = (TextView) findViewById(R.id.textView);
    }

    @Override
    public int getColumnHeight(int columnWidth) {
        return columnWidth + GraphicUtils.convertDpToPixel(30);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.itemview_album_covergrid;
    }

    @Override
    protected void modelChanged() {
        Glide.clear(imageView);
        imageView.setImageDrawable(null);
        videoIcon.setVisibility(media().isVideo() ? VISIBLE : GONE);
        textView.setText(album().name);

        Glide.with(getContext())
                .load(SharedObject.toFullMediaUrl(media().images.low_resolution.url))
                .into(imageView);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private Album album() {
        return (Album) getModel();
    }

    private Media media() {
        return album().getCover().media;
    }
}
