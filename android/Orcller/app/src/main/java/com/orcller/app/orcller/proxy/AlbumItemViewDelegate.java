package com.orcller.app.orcller.proxy;

import android.view.View;
import android.widget.Toast;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.AlbumHeartListActivity;
import com.orcller.app.orcller.activity.AlbumStarListActivity;
import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.activity.CoeditViewActivity;
import com.orcller.app.orcller.activity.CommentListActivity;
import com.orcller.app.orcller.activity.PageListActivity;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.itemview.AlbumItemView;
import com.orcller.app.orcller.manager.AlbumOptionsManager;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.ext.PSObject;
import pisces.psfoundation.utils.Log;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/10/15.
 */
public class AlbumItemViewDelegate extends PSObject implements AlbumItemView.Delegate {
    public static final int COMMENT_ACTION_OPEN_ALBUM_VIEW = 1;
    public static final int COMMENT_ACTION_FOCUS_COMMENT = 2;
    public static final int COMMENT_ACTION_OPEN_COMMENTS = 3;
    private int commentActionType = COMMENT_ACTION_OPEN_ALBUM_VIEW;
    private Invoker invoker;
    private AlbumFlipView playedAlbumFlipView;

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

    public void pauseAlbumFlipView() {
        if (playedAlbumFlipView != null) {
            playedAlbumFlipView.pause();
            playedAlbumFlipView = null;
        }
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * AlbumItemView delegate
     */
    public void onAlbumInfoSynchronize(AlbumItemView itemView, AlbumAdditionalListEntity model) {
        invoker.invalidateOptionsMenu();
        invoker.onAlbumInfoSynchronize(itemView, model);
    }

    public void onAlbumSynchronize(AlbumItemView itemView) {
        invoker.invalidateOptionsMenu();
        invoker.onAlbumSynchronize(itemView);
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
        } else if (AlbumItemView.ButtonType.Control.equals(type)) {
            if (itemView.getAlbumFlipView().isPlaying())
                itemView.getAlbumFlipView().pause();
            else
                itemView.getAlbumFlipView().play();
        }
    }

    public void onPageChange(AlbumItemView itemView) {
    }

    public void onCancelPanning(AlbumItemView itemView, AlbumFlipView view) {
        invoker.onChangePanningState(false);
    }

    public void onChangePageIndex(AlbumItemView itemView, AlbumFlipView view, int pageIndex) {
        invoker.onChangePanningState(false);
    }

    public void onLoadRemainPages(AlbumItemView itemView, AlbumFlipView view) {
    }

    public void onPause(AlbumItemView itemView, AlbumFlipView view) {
        playedAlbumFlipView = null;
    }

    public void onPlay(AlbumItemView itemView, AlbumFlipView view) {
        pauseAlbumFlipView();

        playedAlbumFlipView = view;
    }

    public void onStartLoadRemainPages(AlbumItemView itemView, AlbumFlipView view) {
    }

    public void onStartPanning(AlbumItemView itemView, AlbumFlipView view, FlipView flipView) {
        invoker.onChangePanningState(true);
    }

    public void onStop(AlbumItemView itemView, AlbumFlipView view) {
    }

    public void onTap(AlbumItemView itemView, AlbumFlipView view) {
        invoker.onTap(view);
    }

    public void onTapFlipView(AlbumItemView itemView, AlbumFlipView view, FlipView flipView, PageView pageView) {
        invoker.onTapFlipView(view, flipView, pageView);
        PageListActivity.show(view.getModel(), view.getModel().pages.getPageIndex(pageView.getModel()));
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void doCommentAction(Album album) {
        if (commentActionType == COMMENT_ACTION_OPEN_ALBUM_VIEW) {
            AlbumViewActivity.show(album, true);
        } else if (commentActionType == COMMENT_ACTION_FOCUS_COMMENT && invoker.getCommentInputView() != null) {
            invoker.getCommentInputView().setFocus();
        } else if (commentActionType == COMMENT_ACTION_OPEN_COMMENTS) {
            CommentListActivity.show(album);
        }
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

    public interface Invoker {
        CommentInputView getCommentInputView();
        void invalidateOptionsMenu();
        void onAlbumInfoSynchronize(AlbumItemView itemView, AlbumAdditionalListEntity model);
        void onAlbumSynchronize(AlbumItemView itemView);
        void onChangePanningState(boolean isPanning);
        void onTap(AlbumFlipView view);
        void onTapFlipView(AlbumFlipView view, FlipView flipView, PageView pageView);
    }
}
