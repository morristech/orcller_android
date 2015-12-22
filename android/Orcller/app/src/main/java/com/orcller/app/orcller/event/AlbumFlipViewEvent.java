package com.orcller.app.orcller.event;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 12/23/15.
 */
public class AlbumFlipViewEvent extends Event {
    public static final String CANCEL_PANNING = "cancelPanning";
    public static final String PAGE_INDEX_CHANGE = "pageIndexChange";
    public static final String START_PANNING = "startPanning";

    public AlbumFlipViewEvent(String type, Object target) {
        super(type, target);
    }
}
