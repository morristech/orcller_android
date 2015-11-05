package com.orcller.app.orcllermodules.model.api;

import com.google.gson.annotations.SerializedName;
import com.orcller.app.orcllermodules.model.APIResult;
import com.orcller.app.orcllermodules.model.User;

/**
 * Created by pisces on 11/5/15.
 */
public class ApiUser {
    public class Profile extends APIResult {
        public User entity;
    }
}
