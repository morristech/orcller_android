package com.orcller.app.orcller.model.api;

import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Comments;
import com.orcller.app.orcller.model.Favorites;
import com.orcller.app.orcller.model.Likes;
import com.orcller.app.orcller.model.Pages;
import com.orcller.app.orcllermodules.model.ApiResult;

/**
 * Created by pisces on 11/16/15.
 */
public class ApiAlbum {
    public static class AlbumRes extends ApiResult<Album> {
    }

    public static class CommentsRes extends ApiResult<Comments> {
    }

    public static class FavoritesRes extends ApiResult<Favorites> {
    }

    public static class LikesRes extends ApiResult<Likes> {
    }

    public static class PagesRes extends ApiResult<Pages> {
    }
}
