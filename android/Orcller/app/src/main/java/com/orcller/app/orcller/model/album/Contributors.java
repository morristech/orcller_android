package com.orcller.app.orcller.model.album;

import java.util.List;

/**
 * Created by pisces on 11/16/15.
 */
public class Contributors extends ListEntity {
    public int contributor_id;
    public Contributor.Status contributor_status;
    public List<Contributor> contributors;

    public String getUserIds() {
        return null;
    }
}
