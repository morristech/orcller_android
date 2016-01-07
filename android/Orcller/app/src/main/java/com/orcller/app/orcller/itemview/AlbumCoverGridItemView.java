package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.widget.MediaThumbView;

import pisces.psfoundation.utils.GraphicUtils;

/**
 * Created by pisces on 12/11/15.
 */
public class AlbumCoverGridItemView extends AbstractDataGridItemView {
    private MediaThumbView mediaThumbView;
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

        mediaThumbView = (MediaThumbView) findViewById(R.id.mediaThumbView);
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
        mediaThumbView.setModel(media());
        textView.setText(album().name);
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
