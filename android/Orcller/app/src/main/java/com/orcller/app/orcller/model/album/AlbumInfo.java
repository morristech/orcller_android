package com.orcller.app.orcller.model.album;

import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;

import java.util.Date;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/15/15.
 */
public class AlbumInfo extends Model {
    public long id;
    public long user_uid;
    public long created_time;
    public String user_id;
    public String user_link;
    public String user_name;
    public String user_picture;
    public User user;

    public boolean isMine() {
        if (AuthenticationCenter.getDefault().getUser() == null)
            return false;
        return AuthenticationCenter.getDefault().getUser().user_uid == user_uid;
    }
}
