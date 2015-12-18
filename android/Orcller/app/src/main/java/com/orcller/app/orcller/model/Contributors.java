package com.orcller.app.orcller.model;

/**
 * Created by pisces on 11/16/15.
 */
public class Contributors extends AlbumAdditionalListEntity<Contributor> {
    public int contributor_status;
    public String contributor_id;

    public Contributor getContributor(long userId) {
        for (Contributor contributor : data) {
            if (contributor.user_uid == userId)
                return contributor;
        }
        return null;
    }
}
