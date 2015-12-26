package com.orcller.app.orcller.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Comments;
import com.orcller.app.orcller.model.Page;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.PSButton;

/**
 * Created by pisces on 12/3/15.
 */
public class PageScrollView extends PSLinearLayout
        implements CommentListView.Delegate, MediaView.Delegate, View.OnClickListener {
    private boolean editEnabled;
    private Page model;
    private Delegate delegate;
    private ScrollView scrollView;
    private FrameLayout pageContainer;
    private LinearLayout buttonContainer;
    private AlbumInfoProfileView albumInfoProfileView;
    private PSButton commentButton;
    private PSButton heartButton;
    private MediaScrollView mediaScrollView;
    private DescriptionInputView descriptionInputView;
    private PageCommentListView commentListView;

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

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        pageContainer = (FrameLayout) findViewById(R.id.pageContainer);
        buttonContainer = (LinearLayout) findViewById(R.id.buttonContainer);
        albumInfoProfileView = (AlbumInfoProfileView) findViewById(R.id.albumInfoProfileView);
        mediaScrollView = (MediaScrollView) findViewById(R.id.mediaScrollView);
        commentButton = (PSButton) findViewById(R.id.commentButton);
        heartButton = (PSButton) findViewById(R.id.heartButton);
        descriptionInputView = (DescriptionInputView) findViewById(R.id.descriptionInputView);
        commentListView = (PageCommentListView) findViewById(R.id.commentListView);

        mediaScrollView.setMediaViewDelegate(this);
        mediaScrollView.setScaleAspectFill(true);
        commentButton.setOnClickListener(this);
        heartButton.setOnClickListener(this);
        commentListView.setDelegate(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        pageContainer.getLayoutParams().height = widthMeasureSpec;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * OnClick listener
     */
    public void onClick(View v) {
        if (delegate == null)
            return;

        if (commentButton.equals(v)) {
            delegate.onClickCommentButton(this);
        } else if (heartButton.equals(v)) {
            delegate.onClickHeartButton(this);
        }
    }

    /**
     * CommentListView delegate
     */
    public void onChange(Comments comments) {
        model.comments.participated = comments.participated;
        model.comments.total_count = comments.total_count;

        updateButtons();
    }

    public void onFailure(CommentListView listView, Error error) {
        ProgressBarManager.hide((ViewGroup) listView.getParent());
    }

    public void onLoad(CommentListView listView) {
        if (listView.isFirstLoading())
            ProgressBarManager.show((ViewGroup) listView.getParent());
    }

    public void onLoadComplete(CommentListView listView) {
        ProgressBarManager.hide((ViewGroup) listView.getParent());
    }

    /**
     * MediaView delegate
     */
    public void onClick(MediaView view) {
        if (delegate != null)
            delegate.onClickMediaView(this, view);
    }

    public void onCompleteImageLoad(MediaView view) {
    }

    public void onError(MediaView view) {
    }

    public void onStartImageLoad(MediaView view) {
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

    public DescriptionInputView getDescriptionInputView() {
        return descriptionInputView;
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

    public Page getModel() {
        return model;
    }

    public void setModel(Page model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        reload();
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

    public void addComments(Comments comments) {
        commentListView.add(comments);
        scrollView.pageScroll(View.FOCUS_DOWN);
    }

    public void reload() {
        mediaScrollView.setModel(model.media);
        buttonContainer.setVisibility(model.id > 0 ? VISIBLE : GONE);
        albumInfoProfileView.setModel(model);
        descriptionInputView.setModel(model.getUser());
        descriptionInputView.setText(model.desc);
        commentListView.setPage(model);
        updateButtons();
        editEnabledChanged();
    }

    public void reloadDescription() {
        descriptionInputView.setText(model.desc);
    }

    public void updateButtons() {
        heartButton.setSelected(model.likes.isParticipated());
        heartButton.setText(model.likes.total_count > 0 ? String.valueOf(model.likes.total_count) : null);
        commentButton.setText(model.comments.total_count > 0 ? String.valueOf(model.comments.total_count) : null);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof Model.Event) {
            Model.Event casted = (Model.Event) event;

            if (casted.getType().equals(Model.Event.SYNCHRONIZE)) {
                if (casted.getTarget().equals(model)) {
                    reload();
                } else if (casted.getTarget().equals(model.likes) || casted.getTarget().equals(model.comments)) {
                    updateButtons();
                }
            }
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void editEnabledChanged() {
        mediaScrollView.setScaleEnabled(!editEnabled);
        buttonContainer.setVisibility(editEnabled ? GONE : VISIBLE);
        albumInfoProfileView.setVisibility(editEnabled ? GONE : VISIBLE);
        commentListView.setVisibility(editEnabled ? GONE : VISIBLE);
        descriptionInputView.setEnabled(editEnabled);
        descriptionInputView.setVisibility(editEnabled || !TextUtils.isEmpty(model.desc) ? VISIBLE : GONE);
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public static interface Delegate {
        void onClickCommentButton(PageScrollView target);
        void onClickHeartButton(PageScrollView target);
        void onClickMediaView(PageScrollView target, MediaView mediaView);
    }
}
