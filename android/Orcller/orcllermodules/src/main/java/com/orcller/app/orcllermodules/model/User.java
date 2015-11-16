package com.orcller.app.orcllermodules.model;

import com.orcller.app.orcllermodules.managers.AuthenticationCenter;

import java.util.Date;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/5/15.
 */
public class User extends Model {
    public boolean following;
    public int created_time;
    public int updated_time;
    public long user_uid;
    public String user_id;
    public String user_link;
    public String user_name;
    public String user_picture;
    public String user_profile_message;
    public UserOptions user_options;

    public Date getCreatedTime() {
        return new Date(created_time*1000L);
    }

    public Date getUpdatedTime() {
        return new Date(updated_time*1000L);
    }

    public String getUserUID() {
        return isMe() ? "me" : String.valueOf(user_uid);
    }

    public boolean isMe() {
        if (AuthenticationCenter.getDefault().getUser() == null)
            return false;
        return AuthenticationCenter.getDefault().getUser().user_uid == user_uid;
    }

    public class UserOptions extends Model {
        public boolean following;
        public int album_permission;
        public int follow_count;
        public int follower_count;
        public int pns_types;
    }
}
