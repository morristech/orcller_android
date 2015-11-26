package com.orcller.app.orcllermodules.model.facebook;

import java.util.List;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/26/15.
 */
public class FBVideo extends Model {
    public String id;
    public String description;
    public String picture;
    public String source;
    public String created_time;
    public String updated_time;
    public Thumbnails thumbnails;

    public static class Thumbnails extends Model {
        public List<FBVideoThumbnail> data;
    }
}
