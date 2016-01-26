package com.orcller.app.orcller.event;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 12/2/15.
 */
public class PageListEvent extends Event {
    public static final String PAGE_DELETE_COMPLETE = "pageDeleteComplete";
    public static final String PAGE_ORDER_CHANGE_COMPLETE = "pageOrderChangeComplete";
    public static final String PAGE_DEFAULT_CHANGE_COMPLETE = "pageDefaultChangeComplete";

    public PageListEvent(String type, Object target, Object object) {
        super(type, target, object);
    }
}
