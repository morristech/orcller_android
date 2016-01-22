package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.text.TextUtils;
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
import com.orcller.app.orcller.widget.MediaView;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;
import pisces.psuikit.widget.PSButton;

/**
 * Created by pisces on 12/17/15.
 */
public class TempAlbumItemView extends PSLinearLayout
        implements MediaUploadUnit.Delegate, View.OnClickListener {
    private boolean unitChanged;
    private MediaUploadUnit unit;
    private LinearLayout errorContainer;
    private TextView errorTextView;
    private TextView descriptionTextView;
    private PSButton deleteButton;
    private PSButton retryButton;
    private ProgressBar progressBar;
    private AlbumInfoProfileView albumInfoProfileView;
    private AlbumFlipView albumFlipView;
    private Delegate delegate;

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
    protected void commitProperties() {
        if (unitChanged) {
            unitChanged = false;

            unit.setDelegate(this);
            albumInfoProfileView.setModel(unit.getModel());
            albumFlipView.setPageWidth(getAlbumHeight());
            albumFlipView.setPageHeight(getAlbumHeight());
            albumFlipView.setImageLoadType(unit.getModel().pages.total_count > 1 ?
                    MediaView.ImageLoadType.LowResolution.value() :
                    MediaView.ImageLoadType.Thumbnail.value() | MediaView.ImageLoadType.StandardResoultion.value());
            albumFlipView.setModel(unit.getModel());
            albumFlipView.setPageIndex(unit.getModel().default_page_index);
            albumFlipView.getLayoutParams().width = Application.getWindowWidth();
            albumFlipView.getLayoutParams().height = getAlbumHeight();
            descriptionTextView.setText(unit.getModel().desc);
            descriptionTextView.setVisibility(TextUtils.isEmpty(unit.getModel().desc) ? GONE : VISIBLE);
        }
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_temp_album, this);

        errorContainer = (LinearLayout) findViewById(R.id.errorContainer);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        retryButton = (PSButton) findViewById(R.id.retryButton);
        deleteButton = (PSButton) findViewById(R.id.deleteButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        albumInfoProfileView = (AlbumInfoProfileView) findViewById(R.id.albumInfoProfileView);
        albumFlipView = (AlbumFlipView) findViewById(R.id.albumFlipView);

        albumInfoProfileView.setBackgroundResource(R.drawable.background_bordered_white);
        setDescriptionMode(AlbumInfoProfileView.ALBUM_NAME);
        retryButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (hitTest(retryButton, event) || hitTest(deleteButton, event))
            return super.dispatchTouchEvent(event);
        return false;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public int getDescriptionMode() {
        return albumInfoProfileView.getDescriptionMode();
    }

    public void setDescriptionMode(int descriptionMode) {
        albumInfoProfileView.setDescriptionMode(descriptionMode);
    }

    public MediaUploadUnit getUnit() {
        return unit;
    }

    public void setUnit(MediaUploadUnit unit) {
        if (ObjectUtils.equals(unit, this.unit))
            return;

        this.unit = unit;
        unitChanged = true;

        invalidateProperties();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private int getAlbumHeight() {
        return unit.getModel().pages.count > 1 ? Application.getWindowWidth() / 2 : Application.getWindowWidth();
    }

    private boolean hitTest(View view, MotionEvent event) {
        return event.getX() >= view.getX() &&
                event.getX() <= view.getX() + view.getWidth() &&
                event.getY() >= view.getY() &&
                event.getY() <= view.getY() + view.getHeight();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * View.OnClickListener
     */
    public void onClick(View v) {
        if (retryButton.equals(v)) {
            progressBar.setProgress(0);
            MediaManager.getDefault().startUploading(unit.getModel());
        } else if (deleteButton.equals(v)) {
            if (delegate != null)
                delegate.onClickDeleteButton(this);
        }
    }

    /**
     * MediaUploadUnit.Delegate
     */
    public void onCompleteUploading(MediaUploadUnit unit) {
    }

    public void onFailUploading(MediaUploadUnit unit) {
        errorTextView.setText(Application.isNetworkConnected() ? R.string.m_fail_post_an_error : R.string.m_fail_post_no_connection);
        progressBar.setVisibility(GONE);
        errorContainer.setVisibility(VISIBLE);
    }

    public void onProcessUploading(MediaUploadUnit unit) {
        progressBar.setProgress(Math.round(unit.getProgress() * 100));
    }

    public void onStartUploading(MediaUploadUnit unit) {
        errorContainer.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        progressBar.setProgress(Math.round(unit.getProgress() * 100));
    }

    // ================================================================================================
    //  Delegate
    // ================================================================================================

    public interface Delegate {
        void onClickDeleteButton(TempAlbumItemView itemView);
    }
}
