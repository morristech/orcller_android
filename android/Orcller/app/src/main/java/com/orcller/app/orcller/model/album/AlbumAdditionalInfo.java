package com.orcller.app.orcller.model.album;

import java.util.Date;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/16/15.
 */
public class AlbumAdditionalInfo extends Model {
    public int id;
    public long user_uid;
    public String user_id;
    public String user_name;
    public String user_picture;
    public Date create_time;
}
