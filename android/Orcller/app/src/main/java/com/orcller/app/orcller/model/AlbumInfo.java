package com.orcller.app.orcller.model;

import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/15/15.
 */
public class AlbumInfo extends Model {
    public long id;
    public long user_uid;
    public long created_time;
    public long updated_time;
    public String user_id;
    public String user_link;
    public String user_name;
    public String user_picture;
    protected User user;

    public AlbumInfo() {
        EventBus.getDefault().register(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public User getUser() {
        if (user == null) {
            if (isMine()) {
                user = AuthenticationCenter.getDefault().getUser();
            } else {
                user = new User();
                user.user_uid = user_uid;
                user.user_id = user_id;
                user.user_link = user_link;
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

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (!isMine() || !(event instanceof Model.Event))
            return;

        Model.Event casted = (Model.Event) event;

        if (Model.Event.SYNCHRONIZE.equals(casted.getType()) &&
                AuthenticationCenter.getDefault().getUser().equals(casted.getTarget())) {
            User model = (User) casted.getTarget();
            user_name = model.user_name;
            user_picture = model.user_picture;

            if (user != null) {
                user.user_name = model.user_name;
                user.user_picture = model.user_picture;
            }

            EventBus.getDefault().post(new Event(this));
        }
    }

    // ================================================================================================
    //  Class: Event
    // ================================================================================================

    public static class Event extends pisces.psfoundation.event.Event {
        public Event(Object target) {
            super(target);
        }
    }
}
