package com.orcller.app.orcller.model.album;

import java.util.Date;

/**
 * Created by pisces on 11/16/15.
 */
public class Page extends AlbumInfo {
    public int order;
    public long updated_time;
    public String desc;
    public Media media;
    public Comments comments;
    public Likes likes;
}