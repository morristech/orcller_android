package com.orcller.app.orcller.model.api;

import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.ListEntity;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcllermodules.model.ApiResult;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 12/11/15.
 */
public class ApiUsers {
    public static class AlbumListRes extends ApiResult<AlbumList> {
    }

    public static class CountRes extends ApiResult<Counts> {
    }

    public static class MediaListRes extends ApiResult<MediaList> {
    }

    public class AlbumList extends ListEntity<Album> {
    }

    public class MediaList extends ListEntity<Media> {
    }

    public class Counts extends Model {
        public int album;
        public int coediting;
        public int favorites;
        public int media;
    }
}