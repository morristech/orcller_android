package com.orcller.app.orcller.event;

import com.orcller.app.orcller.model.Contributors;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 12/14/15.
 */
public class CoeditEvent extends Event {
    public static final String CHANGE = "change";
    public static final String SYNC = "sync";
    private Contributors contributors;

    public CoeditEvent(String type, Object target, Contributors contributors) {
        super(type, target);

        this.contributors = contributors;
    }

    public Contributors getContributors() {
        return contributors;
    }
}