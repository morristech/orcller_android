package com.orcller.app.orcller.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Page;

import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;
import pisces.psuikit.ext.PSScrollView;
import pisces.psuikit.widget.PSButton;

/**
 * Created by pisces on 12/3/15.
 */
public class PageScrollView extends PSLinearLayout implements View.OnClickListener {
    private boolean editEnabled;
    private Page model;
    private Delegate delegate;
    private FrameLayout pageContainer;
    private LinearLayout buttonContainer;
    private PageProfileView pageProfileView;
    private PSButton commentButton;
    private PSButton heartButton;
    private MediaScrollView mediaScrollView;
    private DescriptionInputView descriptionInputView;

    public PageScrollView(Context context) {
        super(context);
    }

    public PageScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_pagescroll, this);

        pageContainer = (FrameLayout) findViewById(R.id.pageContainer);
        buttonContainer = (LinearLayout) findViewById(R.id.buttonContainer);
        pageProfileView = (PageProfileView) findViewById(R.id.pageProfileView);
        mediaScrollView = (MediaScrollView) findViewById(R.id.mediaScrollView);
        commentButton = (PSButton) findViewById(R.id.commentButton);
        heartButton = (PSButton) findViewById(R.id.heartButton);
        descriptionInputView = (DescriptionInputView) findViewById(R.id.descriptionInputView);

        mediaScrollView.setScaleAspectFill(true);
        mediaScrollView.setScaleEnabled(false);
        commentButton.setOnClickListener(this);
        heartButton.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        pageContainer.getLayoutParams().height = widthMeasureSpec;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onClick(View v) {
        if (delegate == null)
            return;

        if (commentButton.equals(v)) {
            delegate.onClickCommentButton(this);
        } else if (heartButton.equals(v)) {
            delegate.onClickHeartButton(this);
        }
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

    public boolean isEditEnabled() {
        return editEnabled;
    }

    public void setEditEnabled(boolean editEnabled) {
        if (editEnabled == this.editEnabled)
            return;

        this.editEnabled = editEnabled;

        editEnabledChanged();
    }

    public ImageMediaScrollView getImageMediaScrollView() {
        return mediaScrollView.getImageMediaScrollView();
    }

    public VideoMediaView getVideoMediaView() {
        return mediaScrollView.getVideoMediaView();
    }

    public boolean hasImageMediaScrollView() {
        return getImageMediaScrollView() != null;
    }

    public boolean hasVideoMediaView() {
        return getVideoMediaView() != null;
    }

    public Page getModel() {
        return model;
    }

    public void setModel(Page model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        reload();
    }

    public void reload() {
        mediaScrollView.setModel(model.media);
        buttonContainer.setVisibility(model.id > 0 ? VISIBLE : GONE);
        heartButton.setSelected(model.likes.getParticipated());
        heartButton.setText(model.likes.total_count > 0 ? String.valueOf(model.likes.total_count) : null);
        commentButton.setText(model.comments.total_count > 0 ? String.valueOf(model.comments.total_count) : null);
        pageProfileView.setModel(model);
        descriptionInputView.setModel(model.getUser());
        descriptionInputView.setText(model.desc);
        editEnabledChanged();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void editEnabledChanged() {
        buttonContainer.setVisibility(editEnabled ? GONE : VISIBLE);
        pageProfileView.setVisibility(editEnabled ? GONE : VISIBLE);
        descriptionInputView.setEnabled(editEnabled);
        descriptionInputView.setVisibility(editEnabled || !TextUtils.isEmpty(model.desc) ? VISIBLE : GONE);
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public static interface Delegate {
        void onClickCommentButton(PageScrollView target);
        void onClickHeartButton(PageScrollView target);
    }
}
