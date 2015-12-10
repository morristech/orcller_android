package com.orcller.app.orcllermodules.model;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/5/15.
 */
public class User extends BaseUser {
    private long created_time;
    private long updated_time;
    public String user_profile_message;
    public UserOptions user_options;

    public class UserOptions extends Model {
        public boolean following;
        public int album_permission;
        public int follow_count;
        public int follower_count;
        public int pns_types;
    }
}
