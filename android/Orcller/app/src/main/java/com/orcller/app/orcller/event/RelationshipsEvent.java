package com.orcller.app.orcller.event;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 12/24/15.
 */
public class RelationshipsEvent extends Event {
    public static final String FOLLOW = "follow";
    public static final String UNFOLLOW = "unfollow";

    public RelationshipsEvent(String type, Object target) {
        super(type, target);
    }
}
