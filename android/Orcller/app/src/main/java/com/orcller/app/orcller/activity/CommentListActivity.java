package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Comments;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.CommentListView;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

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

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        commentListView = (CommentListView) findViewById(R.id.commentListView);
        commentInputView = (CommentInputView) findViewById(R.id.commentInputView);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);
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
        params.bottomMargin = commentInputView.isShown() ? commentInputView.getHeight() : 0;
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
     * CommentListView delegate
     */
    public void onChange(Comments comments) {
        model.comments.participated = comments.participated;
        model.comments.total_count = comments.total_count;

        updateTitle();
    }

    /**
     * CommentInputView delegate
     */
    public void onClickPostButton() {
        if (invalidDataLoading())
            return;

        commentInputView.clearFocus();
        ProgressBarManager.show(this);

        String message = commentInputView.getText().toString().trim();
        final Runnable error = new Runnable() {
            @Override
            public void run() {
                AlertDialogUtils.retry(R.string.m_fail_comment, new Runnable() {
                    @Override
                    public void run() {
                        onClickPostButton();
                    }
                });
            }
        };

        AlbumDataProxy.getDefault().comment(model.id, message, new Callback<ApiAlbum.CommentsRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.CommentsRes> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    commentInputView.clear();
                    commentListView.add(response.body().entity);
                    model.comments.synchronize(response.body().entity, new Runnable() {
                        @Override
                        public void run() {
                            updateTitle();
                            commentListView.scrollTo(0, 0);
                        }
                    });
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());

                    error.run();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                endDataLoading();
                error.run();
            }
        });
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
    }

    private void updateTitle() {
        getSupportActionBar().setTitle(SharedObject.getAlbumInfoText(model.comments));
    }
}
