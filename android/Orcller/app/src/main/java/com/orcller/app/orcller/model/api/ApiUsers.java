package com.orcller.app.orcller.model.api;

import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.AlbumCoedit;
import com.orcller.app.orcller.model.Coedit;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcllermodules.model.ApiResult;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 12/11/15.
 */
public class ApiUsers {
    public static class AlbumCoeditListRes extends ApiResult<AlbumCoeditList> {
    }

    public static class AlbumListRes extends ApiResult<AlbumList> {
    }

    public static class CoeditListRes extends ApiResult<CoeditList> {
    }

    public static class CountRes extends ApiResult<Counts> {
    }

    public static class MediaListRes extends ApiResult<MediaList> {
    }

    public static class NewUserPictureNameRes extends ApiResult<UserPictureName> {
    }

    public class AlbumCoeditList extends ListEntity<AlbumCoedit> {
    }

    public class AlbumList extends ListEntity<Album> {
    }

    public class CoeditList extends ListEntity<Coedit> {
    }

    public class MediaList extends ListEntity<Media> {
    }

    public class Counts extends Model {
        public int album;
        public int coediting;
        public int favorites;
        public int media;
    }

    public class UserPictureName extends Model {
        public String user_picture;
    }
}