package com.orcller.app.orcller.event;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 11/16/15.
 */
public class AlbumEvent extends Event {
    public static final String CREATE = "create";
    public static final String DELETE = "delete";
    public static final String MODIFY = "modify";

    public AlbumEvent(String type, Object target, Object object) {
        super(type, target, object);
    }
}
