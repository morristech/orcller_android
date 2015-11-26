package com.orcller.app.orcllermodules.model.facebook;

import android.os.Bundle;

import java.util.List;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/26/15.
 */
public class FBAlbum extends Model {
    protected static final int ALBUM_CONTENT_LIMIT_COUNT = 50;
    public int count;
    public String id;
    public String name;
    public String created_time;
    public String updated_time;
    public CoverPhoto cover_photo;

    public String getGraphPath() {
        return String.valueOf(id) + "/photos";
    }

    public Bundle getParameters() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,images,created_time,updated_time");
        parameters.putInt("limit", ALBUM_CONTENT_LIMIT_COUNT);
        return parameters;
    }

    public static class CoverPhoto extends Model {
        public String created_time;
        public String id;
        public String name;
    }
}
