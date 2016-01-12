package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.AlbumItemView;
import com.orcller.app.orcller.manager.AlbumOptionsManager;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.Comments;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.AlbumItemViewDelegate;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.CommentListView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import pisces.psfoundation.model.Resources;
import pisces.psuikit.keyboard.SoftKeyboardNotifier;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSScrollView;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.ExceptionView;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/7/15.
 */
public class AlbumViewActivity extends PSActionBarActivity
        implements AlbumItemViewDelegate.Invoker, CommentInputView.Delegate,
        CommentListView.Delegate, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String ALBUM_KEY = "album";
    private static final String ALBUM_ID_KEY = "album_id";
    private static final String ALLOWS_COMMENT_INPUT_FOCUS = "allowsCommentInputFocus";
    private Error loadError;
    private Album model;
    private AlbumOptionsManager albumOptionsManager;
    private AlbumItemViewDelegate albumItemViewDelegate;
    private LinearLayout rootLayout;
    private RelativeLayout container;
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
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        container = (RelativeLayout) findViewById(R.id.container);
        scrollView = (PSScrollView) findViewById(R.id.scrollView);
        albumItemView = (AlbumItemView) findViewById(R.id.albumItemView);
        commentListView = (CommentListView) findViewById(R.id.commentListView);
        commentInputView = (CommentInputView) findViewById(R.id.commentInputView);
        albumItemViewDelegate = new AlbumItemViewDelegate(this);

        exceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.DoseNotExistAlbum, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NoPermissionForAlbum, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, container));
        albumItemViewDelegate.setCommentActionType(AlbumItemViewDelegate.COMMENT_ACTION_FOCUS_COMMENT);
        albumItemView.setDelegate(albumItemViewDelegate);
        commentListView.setDelegate(this);
        commentInputView.setDelegate(this);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        SoftKeyboardNotifier.getDefault().register(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        load();
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
        ProgressBarManager.hide();
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide();
        exceptionViewManager.validate();
    }

    @Override
    public void onClick(ExceptionView view) {
        if (ExceptionViewFactory.Type.NetworkError.equals(view.getTag()) &&
                ExceptionViewFactory.Type.UnknownError.equals(view.getTag())) {
            exceptionViewManager.clear();
            load();
        }
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.DoseNotExistAlbum.equals(view.getTag()))
            return loadError instanceof APIError && ((APIError) loadError).getCode() == APIError.APIErrorCodeAlbumDoesNotExist;

        if (ExceptionViewFactory.Type.NoPermissionForAlbum.equals(view.getTag()))
            return loadError instanceof APIError && ((APIError) loadError).getCode() == APIError.APIErrorCodeNoPermissionForAlbum;

        if (ExceptionViewFactory.Type.NetworkError.equals(view.getTag()))
            return !Application.isNetworkConnected();

        if (ExceptionViewFactory.Type.UnknownError.equals(view.getTag()))
            return loadError != null;

        return false;
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
     * CommentListView.Delegate
     */
    public void onChange(Comments comments) {
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
     * CommentInputView.Delegate
     */
    public void onCompletePost(CommentInputView commentInputView, Comments comments) {
        commentListView.add(comments);
        scrollView.pageScroll(View.FOCUS_DOWN);
    }

    /**
     * AlbumItemViewDelete.Invoker
     */
    public CommentInputView getCommentInputView() {
        return commentInputView;
    }

    public void onAlbumInfoSynchronize(AlbumItemView itemView, AlbumAdditionalListEntity model) {
    }

    public void onAlbumSynchronize(AlbumItemView itemView) {
    }

    public void onChangePanningState(boolean isPanning) {
        scrollView.setScrollable(!isPanning);
    }

    public void onTap(AlbumFlipView view, FlipView flipView, PageView pageView) {
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load() {
        if (getIntent().getSerializableExtra(ALBUM_KEY) != null) {
            setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
            setAllowsCommentInputFocus(getIntent().getBooleanExtra(ALLOWS_COMMENT_INPUT_FOCUS, false));
        } else if (getIntent().getLongExtra(ALBUM_ID_KEY, 0) > 0) {
            load(getIntent().getLongExtra(ALBUM_ID_KEY, 0));
        } else if (getIntent().getData().getQueryParameter(ALBUM_ID_KEY) != null) {
            load(Long.valueOf(getIntent().getData().getQueryParameter(ALBUM_ID_KEY)));
        }
    }

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

        loadError = null;

        ProgressBarManager.show();

        AlbumDataProxy.getDefault().view(albumId, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    setModel(response.body().entity);
                } else {
                    loadError = new APIError(response.body());

                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());
                }

                endDataLoading();
            }

            @Override
            public void onFailure(Throwable t) {
                loadError = new Error(t.getMessage());

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
        commentInputView.setModel(model.comments, model.id);
        setScrollViewLayout();
        scrollView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    private void setScrollViewLayout() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
        params.bottomMargin = commentInputView.getVisibility() == View.VISIBLE ? commentInputView.getHeight() : 0;
    }
}