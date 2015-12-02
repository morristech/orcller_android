package com.orcller.app.orcller.event;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 12/3/15.
 */
public class MediaEvent extends Event {
    public static final String CHANGE_IMAGES = "changeImages";

    public MediaEvent(String type, Object target, Object object) {
        super(type, target, object);
    }
}
