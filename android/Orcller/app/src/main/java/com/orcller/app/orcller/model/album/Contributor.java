package com.orcller.app.orcller.model.album;

import com.orcller.app.orcllermodules.model.User;

/**
 * Created by pisces on 11/16/15.
 */
public class Contributor extends User {
    public enum Status {
        None,
        Ask,
        Invite,
        Accept
    };

    public String contributor_id;
    public Status contributor_status;
}
