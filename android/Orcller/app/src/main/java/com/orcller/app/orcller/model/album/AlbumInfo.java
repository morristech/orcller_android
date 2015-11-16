package com.orcller.app.orcller.model.album;

import com.orcller.app.orcllermodules.model.User;

import java.util.Date;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/15/15.
 */
public class AlbumInfo extends Model {
    public long id;
    public int user_uid;
    public String user_id;
    public String user_link;
    public String user_name;
    public String user_picture;
    public Date created_time;
    public User user;
}
