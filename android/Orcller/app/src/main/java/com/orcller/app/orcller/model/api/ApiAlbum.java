package com.orcller.app.orcller.model.api;

import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Comments;
import com.orcller.app.orcller.model.album.Favorites;
import com.orcller.app.orcller.model.album.Likes;
import com.orcller.app.orcller.model.album.Pages;
import com.orcller.app.orcllermodules.model.ApiResult;

/**
 * Created by pisces on 11/16/15.
 */
public class ApiAlbum {
    public static class AlbumRes extends ApiResult {
        public Album entity;
    }

    public static class CommentsRes extends ApiResult {
        public Comments entity;
    }

    public static class FavoritesRes extends ApiResult {
        public Favorites entity;
    }

    public static class LikesRes extends ApiResult {
        public Likes entity;
    }

    public static class PagesRes extends ApiResult {
        public Pages entity;
    }
}
