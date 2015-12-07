package com.orcller.app.orcller.model.album;

import java.util.Date;

import pisces.psfoundation.utils.DateUtil;

/**
 * Created by pisces on 11/16/15.
 */
public class Page extends AlbumInfo {
    public int order;
    public String desc;
    public Media media;
    public Comments comments = new Comments();
    public Likes likes = new Likes();

    public static Page create(Media media) {
        Page page = new Page();
        page.created_time = DateUtil.toUnixtimestamp(new Date());
        page.media = media;
        return page;
    }
}