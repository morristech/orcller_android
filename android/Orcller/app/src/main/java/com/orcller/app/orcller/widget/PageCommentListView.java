package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Comment;
import com.orcller.app.orcller.model.album.Page;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;

import pisces.psfoundation.utils.ObjectUtils;
import retrofit.Call;

/**
 * Created by pisces on 12/5/15.
 */
public class PageCommentListView extends CommentListView {
    private Page page;

    public PageCommentListView(Context context) {
        super(context);
    }

    public PageCommentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageCommentListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Pa getPage() {
        return page;
    }

    public void setPage(Page page) {
        if (ObjectUtils.equals(page, this.page))
            return;

        this.page = page;

        if (page.id > 0)
            refresh();
        else
            clear();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected Call<ApiAlbum.CommentsRes> createCommentsCall(int limit, String prev) {
        return AlbumDataProxy.getDefault().service().commentsOfPage(page.id, limit, prev);
    }

    protected Call<ApiAlbum.CommentsRes> createUncommentCall(Comment comment) {
        return AlbumDataProxy.getDefault().service().uncommentOfPage(page.id, comment.id);
    }
}
