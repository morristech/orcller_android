package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.widget.MediaThumbView;

/**
 * Created by pisces on 12/11/15.
 */
public class MediaGridItemView extends AbstractDataGridItemView {
    private MediaThumbView mediaThumbView;

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

        mediaThumbView = (MediaThumbView) findViewById(R.id.mediaThumbView);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.itemview_mediagrid;
    }

    @Override
    protected void modelChanged() {
        mediaThumbView.setModel(media());
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private Media media() {
        return (Media) getModel();
    }
}
