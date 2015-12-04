package com.orcller.app.orcller.model.album;

import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/16/15.
 */
public class AlbumAdditionalInfo extends Model {
    public long id;
    public long created_time;
    public long user_uid;
    public String user_id;
    public String user_name;
    public String user_picture;
    private User user;

    public User getUser() {
        if (user == null) {
            if (isMine()) {
                user = AuthenticationCenter.getDefault().getUser();
            } else {
                user = new User();
                user.user_uid = user_uid;
                user.user_id = user_id;
                user.user_name = user_name;
                user.user_picture = user_picture;
            }
        }
        return user;
    }

    public boolean isMine() {
        if (AuthenticationCenter.getDefault().getUser() == null)
            return false;
        return AuthenticationCenter.getDefault().getUser().user_uid == user_uid;
    }
}
