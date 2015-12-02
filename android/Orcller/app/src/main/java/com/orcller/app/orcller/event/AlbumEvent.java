package com.orcller.app.orcller.event;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 11/16/15.
 */
public class AlbumEvent extends Event {
    public static final String COMPLETE_CREATION = "completeCreation";
    public static final String COMPLETE_MODIFICATION = "completeModification";

    public AlbumEvent(String type, Object target, Object object) {
        super(type, target, object);
    }
}
