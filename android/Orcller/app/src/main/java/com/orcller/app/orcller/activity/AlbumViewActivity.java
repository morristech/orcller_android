package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.itemview.AlbumItemView;
import com.orcller.app.orcller.manager.AlbumOptionsManager;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.CommentListView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GsonUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSScrollView;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/7/15.
 */
public class AlbumViewActivity extends PSActionBarActivity
        implements AlbumItemView.Delegate, CommentInputView.Delegate, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String ALBUM_KEY = "album";
    private static final String ALBUM_ID_KEY = "albumId";
    private static final String ALLOWS_COMMENT_INPUT_FOCUS = "allowsCommentInputFocus";
    private Album model;
    private AlbumOptionsManager albumOptionsManager;
    private LinearLayout rootLayout;
    private PSScrollView scrollView;
    private AlbumItemView albumItemView;
    private CommentListView commentListView;
    private CommentInputView commentInputView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_view);

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        scrollView = (PSScrollView) findViewById(R.id.scrollView);
        albumItemView = (AlbumItemView) findViewById(R.id.albumItemView);
        commentListView = (CommentListView) findViewById(R.id.commentListView);
        commentInputView = (CommentInputView) findViewById(R.id.commentInputView);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);
        albumItemView.setDelegate(this);
        commentInputView.setDelegate(this);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        SoftKeyboardNotifier.getDefault().register(this);
        EventBus.getDefault().register(this);


        //TODO: heart/unheart, star/unstar, co-edit, AlbumItemView에 AlbumOptionsManager 연동
    }

    @Override
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        setScrollViewLayout();

        if (getIntent().getSerializableExtra(ALBUM_KEY) != null) {
            setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
            setAllowsCommentInputFocus(getIntent().getBooleanExtra(ALLOWS_COMMENT_INPUT_FOCUS, false));
        } else if (getIntent().getLongExtra(ALBUM_ID_KEY, 0) > 0) {
            load(getIntent().getLongExtra(ALBUM_ID_KEY, 0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (albumOptionsManager != null)
            return albumOptionsManager.onCreateOptionsMenu(menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return super.onOptionsItemSelected(item);
            default:
                return albumOptionsManager.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        SoftKeyboardNotifier.getDefault().unregister(this);
        ProgressBarManager.hide(this);

        rootLayout = null;
        scrollView = null;
        albumItemView = null;
        commentListView = null;
        commentInputView = null;
        albumOptionsManager = null;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album album, boolean allowsCommentInputFocus) {
        Intent intent = new Intent(Application.applicationContext(), AlbumViewActivity.class);
        intent.putExtra(ALBUM_KEY, album);
        intent.putExtra(ALLOWS_COMMENT_INPUT_FOCUS, allowsCommentInputFocus);
        Application.getTopActivity().startActivity(intent);
    }

    public static void show(long albumId, boolean allowsCommentInputFocus) {
        Intent intent = new Intent(Application.applicationContext(), AlbumViewActivity.class);
        intent.putExtra(ALBUM_ID_KEY, albumId);
        intent.putExtra(ALLOWS_COMMENT_INPUT_FOCUS, allowsCommentInputFocus);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof IndexChangeEvent) {
            IndexChangeEvent casted = (IndexChangeEvent) event;
            albumItemView.getAlbumFlipView().setPageIndex(
                    SharedObject.convertPositionToPageIndex(casted.getSelectedIndex()));
        }
    }

    /**
     * AlbumItemView delegate
     */
    public void onAlbumInfoSynchronize(AlbumItemView itemView, AlbumAdditionalListEntity model) {

    }

    public void onAlbumSynchronize(AlbumItemView itemView) {

    }

    public void onClick(AlbumItemView itemView, AlbumItemView.ButtonType type) {

    }

    public void onPageChange(AlbumItemView itemView) {

    }

    public void onCancelPanning(AlbumFlipView view) {
        scrollView.setScrollable(true);
    }

    public void onChangePageIndex(AlbumFlipView view, int pageIndex) {
        scrollView.setScrollable(true);
    }

    public void onLoadRemainPages(AlbumFlipView view) {
    }

    public void onPause(AlbumFlipView view) {
    }

    public void onStartLoadRemainPages(AlbumFlipView view) {
    }

    public void onStartPanning(AlbumFlipView view) {
        scrollView.setScrollable(false);
    }

    public void onStop(AlbumFlipView view) {
    }

    public void onTap(AlbumFlipView view, FlipView flipView, PageView pageView) {
        PageListActivity.show(model, model.pages.getPageIndex(pageView.getModel()));
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
        final Runnable retry = new Runnable() {
            @Override
            public void run() {
                onClickPostButton();
            }
        };

        AlbumDataProxy.getDefault().comment(model.id, message, new Callback<ApiAlbum.CommentsRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.CommentsRes> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    commentInputView.clear();
                    model.comments.synchronize(response.body().entity);
                    commentListView.add(response.body().entity);
                    scrollView.pageScroll(View.FOCUS_DOWN);
                } else {
                    AlertDialogUtils.retry(R.string.m_fail_comment, retry);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                endDataLoading();
                AlertDialogUtils.retry(R.string.m_fail_comment, retry);
            }
        });
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setAllowsCommentInputFocus(boolean focus) {
        if (focus)
            commentInputView.setFocus();
    }

    private void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    private void load(long albumId) {
        if (invalidDataLoading())
            return;

        ProgressBarManager.show();

        AlbumDataProxy.getDefault().view(albumId, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    setModel(response.body().entity);
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                endDataLoading();

                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);
            }
        });
    }

    private void modelChanged() {
        albumOptionsManager = new AlbumOptionsManager(this, model);
        getSupportActionBar().setTitle(model.name);
        albumItemView.setModel(model);
        commentListView.setModel(model);
        scrollView.setVisibility(View.VISIBLE);
        commentInputView.setVisibility(View.VISIBLE);
        setScrollViewLayout();
        invalidateOptionsMenu();
    }

    private void setScrollViewLayout() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
        params.bottomMargin = commentInputView.isShown() ? commentInputView.getHeight() : 0;
    }
}