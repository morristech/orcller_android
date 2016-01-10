package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.manager.MediaUploadUnit;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.AlbumInfoProfileView;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/17/15.
 */
public class TempAlbumItemView extends PSLinearLayout
        implements MediaUploadUnit.Delegate, View.OnClickListener {
    private MediaUploadUnit unit;
    private LinearLayout errorContainer;
    private TextView textView;
    private Button retryButton;
    private ProgressBar progressBar;
    private AlbumInfoProfileView albumInfoProfileView;
    private AlbumFlipView albumFlipView;

    public TempAlbumItemView(Context context) {
        super(context);
    }

    public TempAlbumItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TempAlbumItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_temp_album, this);

        errorContainer = (LinearLayout) findViewById(R.id.errorContainer);
        textView = (TextView) findViewById(R.id.textView);
        retryButton = (Button) findViewById(R.id.retryButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        albumInfoProfileView = (AlbumInfoProfileView) findViewById(R.id.albumInfoProfileView);
        albumFlipView = (AlbumFlipView) findViewById(R.id.albumFlipView);

        albumInfoProfileView.setBackgroundResource(R.drawable.background_bordered_white);
        albumInfoProfileView.setDescriptionMode(AlbumInfoProfileView.ALBUM_NAME);
        retryButton.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (albumFlipView.getPageWidth() < 1) {
            albumFlipView.setPageWidth(getWidth() / 2);
            albumFlipView.setPageHeight(getWidth() / 2);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getX() >= retryButton.getX() &&
                event.getX() <= retryButton.getX() + retryButton.getWidth() &&
                event.getY() >= retryButton.getY() &&
                event.getY() <= retryButton.getY() + retryButton.getHeight())
            return super.dispatchTouchEvent(event);
        return false;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public MediaUploadUnit getUnit() {
        return unit;
    }

    public void setUnit(MediaUploadUnit unit) {
        if (ObjectUtils.equals(unit, this.unit))
            return;

        this.unit = unit;

        unit.setDelegate(this);
        albumInfoProfileView.setModel(unit.getModel());
        albumFlipView.setModel(unit.getModel());
        albumFlipView.setPageIndex(unit.getModel().default_page_index);
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * View.OnClickListener
     */
    public void onClick(View v) {
        progressBar.setProgress(0);
        MediaManager.getDefault().startUploading(unit.getModel());
    }

    /**
     * MediaUploadUnit.Delegate
     */
    public void onCompleteUploading(MediaUploadUnit unit) {
    }

    public void onFailUploading(MediaUploadUnit unit) {
        textView.setText(Application.isNetworkConnected() ? R.string.m_fail_post_an_error : R.string.m_fail_post_no_connection);
        progressBar.setVisibility(GONE);
        errorContainer.setVisibility(VISIBLE);
    }

    public void onProcessUploading(MediaUploadUnit unit) {
        progressBar.setProgress(Math.round(unit.getProcess() * 100));
    }

    public void onStartUploading(MediaUploadUnit unit) {
        errorContainer.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        progressBar.setProgress(Math.round(unit.getProcess() * 100));
    }
}
