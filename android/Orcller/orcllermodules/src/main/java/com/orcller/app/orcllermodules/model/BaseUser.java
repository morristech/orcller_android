package com.orcller.app.orcllermodules.model;

import com.orcller.app.orcllermodules.managers.AuthenticationCenter;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 12/10/15.
 */
public class BaseUser extends Model {
    public int following;
    public long user_uid;
    public String user_id;
    public String user_link;
    public String user_name;
    public String user_picture;

    public String getUserUID() {
        return isMe() ? "me" : String.valueOf(user_uid);
    }

    public boolean isFollowing() {
        return following > 0;
    }

    public boolean isMe() {
        if (AuthenticationCenter.getDefault().getUser() == null)
            return false;
        return AuthenticationCenter.getDefault().getUser().user_uid == user_uid;
    }
}