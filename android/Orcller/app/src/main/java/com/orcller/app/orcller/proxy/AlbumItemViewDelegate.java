package com.orcller.app.orcller.proxy;

import android.view.View;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.activity.AlbumHeartListActivity;
import com.orcller.app.orcller.activity.AlbumStarListActivity;
import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.activity.CoeditViewActivity;
import com.orcller.app.orcller.activity.CommentListActivity;
import com.orcller.app.orcller.activity.PageListActivity;
import com.orcller.app.orcller.itemview.AlbumItemView;
import com.orcller.app.orcller.manager.AlbumOptionsManager;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;

import pisces.psfoundation.ext.PSObject;
import pisces.psfoundation.utils.Log;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/10/15.
 */
public class AlbumItemViewDelegate extends PSObject implements AlbumItemView.Delegate {
    public static final int COMMENT_ACTION_ALBUM = 1;
    public static final int COMMENT_ACTION_COMMENTS = 1;
    private int commentActionType = COMMENT_ACTION_ALBUM;
    private Invoker invoker;

    public AlbumItemViewDelegate(Invoker invoker) {
        this.invoker = invoker;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public int getCommentActionType() {
        return commentActionType;
    }

    public void setCommentActionType(int commentActionType) {
        this.commentActionType = commentActionType;
    }

    // ================================================================================================
    //  Interface Implemetaion
    // ================================================================================================

    /**
     * AlbumItemView delegate
     */
    public void onAlbumInfoSynchronize(AlbumItemView itemView, AlbumAdditionalListEntity model) {
        invoker.invalidateOptionsMenu();
    }

    public void onAlbumSynchronize(AlbumItemView itemView) {
        invoker.invalidateOptionsMenu();
    }

    public void onClick(AlbumItemView itemView, AlbumItemView.ButtonType type, View view) {
        if (AlbumItemView.ButtonType.Coedit.equals(type)) {
            CoeditViewActivity.show(itemView.getModel());
        } else if (AlbumItemView.ButtonType.Comment.equals(type)) {
            doCommentAction(itemView.getModel());
        } else if (AlbumItemView.ButtonType.CommentList.equals(type)) {
            CommentListActivity.show(itemView.getModel());
        } else if (AlbumItemView.ButtonType.Heart.equals(type)) {
            heart(itemView);
        } else if (AlbumItemView.ButtonType.HeartList.equals(type)) {
            AlbumHeartListActivity.show(itemView.getModel().id);
        } else if (AlbumItemView.ButtonType.Options.equals(type)) {
            new AlbumOptionsManager(itemView.getContext(), itemView.getModel()).onCreateOptionsMenu(view);
        } else if (AlbumItemView.ButtonType.Star.equals(type)) {
            star(itemView);
        } else if (AlbumItemView.ButtonType.StarList.equals(type)) {
            AlbumStarListActivity.show(itemView.getModel().id);
        }
    }

    public void onPageChange(AlbumItemView itemView) {
        itemView.reload();
    }

    public void onCancelPanning(AlbumFlipView view) {
        invoker.onChangePanningState(false);
    }

    public void onChangePageIndex(AlbumFlipView view, int pageIndex) {
        invoker.onChangePanningState(false);
    }

    public void onLoadRemainPages(AlbumFlipView view) {
    }

    public void onPause(AlbumFlipView view) {
    }

    public void onStartLoadRemainPages(AlbumFlipView view) {
    }

    public void onStartPanning(AlbumFlipView view) {
        invoker.onChangePanningState(true);
    }

    public void onStop(AlbumFlipView view) {
    }

    public void onTap(AlbumFlipView view, FlipView flipView, PageView pageView) {
        invoker.onTap(view, flipView, pageView);
        PageListActivity.show(view.getModel(), view.getModel().pages.getPageIndex(pageView.getModel()));
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void doCommentAction(Album album) {
        if (invoker.getCommentInputView() != null)
            invoker.getCommentInputView().setFocus();

        if (commentActionType == COMMENT_ACTION_ALBUM)
            AlbumViewActivity.show(album, true);
        else if (commentActionType == COMMENT_ACTION_COMMENTS)
            CommentListActivity.show(album);
    }

    private void heart(AlbumItemView itemView) {
        if (invalidDataLoading())
            return;

        final Album album = itemView.getModel();

        AlbumDataProxy.getDefault().like(album, new Callback<ApiAlbum.LikesRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.LikesRes> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    album.likes.synchronize(response.body().entity, true);
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                endDataLoading();
            }
        });

        album.likes.participated();
        itemView.updateDisplayList();
    }

    private void star(AlbumItemView itemView) {
        if (invalidDataLoading())
            return;

        final Album album = itemView.getModel();

        AlbumDataProxy.getDefault().favorite(album, new Callback<ApiAlbum.FavoritesRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.FavoritesRes> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    album.favorites.synchronize(response.body().entity, true);
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                endDataLoading();
            }
        });

        album.favorites.participated();
        itemView.updateDisplayList();
    }

    // ================================================================================================
    //  Interface: Invoker
    // ================================================================================================

    public static interface Invoker {
        CommentInputView getCommentInputView();
        void invalidateOptionsMenu();
        void onChangePanningState(boolean isPanning);
        void onTap(AlbumFlipView view, FlipView flipView, PageView pageView);
    }
}
