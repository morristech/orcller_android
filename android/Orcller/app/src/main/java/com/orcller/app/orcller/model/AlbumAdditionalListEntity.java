package com.orcller.app.orcller.model;

/**
 * Created by pisces on 12/4/15.
 */
public class AlbumAdditionalListEntity<T> extends ListEntity<T> {
    public long id;
    public int participated;

    public boolean isParticipated() {
        return participated > 0;
    }

    public void participated() {
        if (isParticipated()) {
            participated = 0;
            count--;
            total_count--;
        } else {
            participated = 1;
            count++;
            total_count++;
        }
    }
}
