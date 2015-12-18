package com.orcller.app.orcller.model.api;

import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.model.User;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 12/12/15.
 */
public class ApiRelationships {
    public class FollowRes extends ApiResult<FollowEntity> {
    }

    public static class UserListRes extends ApiResult<UserList> {
    }

    public class UserList extends ListEntity<User> {
    }

    public static class FollowEntity extends Model {
        public int follow_count;
    }
}
