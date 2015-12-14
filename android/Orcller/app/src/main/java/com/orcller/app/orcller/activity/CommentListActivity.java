package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Comments;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.CommentListView;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 12/10/15.
 */
public class CommentListActivity extends PSActionBarActivity
        implements CommentInputView.Delegate, CommentListView.Delegate, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String ALBUM_KEY = "album";
    private Album model;
    private LinearLayout rootLayout;
    private CommentListView commentListView;
    private CommentInputView commentInputView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment_list);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        commentListView = (CommentListView) findViewById(R.id.commentListView);
        commentInputView = (CommentInputView) findViewById(R.id.commentInputView);

        commentListView.setDelegate(this);
        commentListView.setSortDirection(CommentListView.SortDirection.Top);
        commentInputView.setDelegate(this);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
        SoftKeyboardNotifier.getDefault().register(this);
    }

    @Override
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) commentListView.getLayoutParams();
        params.bottomMargin = commentInputView.getVisibility() == View.VISIBLE ? commentInputView.getHeight() : 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SoftKeyboardNotifier.getDefault().unregister(this);

        rootLayout = null;
        commentListView = null;
        commentInputView = null;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album album) {
        Intent intent = new Intent(Application.applicationContext(), CommentListActivity.class);
        intent.putExtra(ALBUM_KEY, album);
        Application.getTopActivity().startActivity(intent);
    }

    /**
     * CommentListView.Delegate
     */
    public void onChange(Comments comments) {
        model.comments.participated = comments.participated;
        model.comments.total_count = comments.total_count;

        updateTitle();
    }

    /**
     * CommentInputView.Delegate
     */
    public void onCompletePost(CommentInputView commentInputView, Comments comments) {
        commentListView.add(comments);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    private void modelChanged() {
        updateTitle();
        commentListView.setModel(model);
        commentInputView.setModel(model.comments, model.id);
    }

    private void updateTitle() {
        getSupportActionBar().setTitle(SharedObject.getAlbumInfoText(model.comments));
    }
}
