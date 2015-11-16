package com.orcller.app.orcller.model.api;

import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Comments;
import com.orcller.app.orcller.model.album.Favorites;
import com.orcller.app.orcller.model.album.Likes;
import com.orcller.app.orcller.model.album.Pages;
import com.orcller.app.orcllermodules.model.APIResult;

/**
 * Created by pisces on 11/16/15.
 */
public class ApiAlbum {
    public static class AlbumRes extends APIResult {
        public Album entity;
    }

    public static class CommentsRes extends APIResult {
        public Comments entity;
    }

    public static class FavoritesRes extends APIResult {
        public Favorites entity;
    }

    public static class LikesRes extends APIResult {
        public Likes entity;
    }

    public static class PagesRes extends APIResult {
        public Pages entity;
    }
}
