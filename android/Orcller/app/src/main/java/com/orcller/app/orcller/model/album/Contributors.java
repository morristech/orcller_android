package com.orcller.app.orcller.model.album;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pisces on 11/16/15.
 */
public class Contributors extends ListEntity {
    public boolean participated;
    public int contributor_status;
    public String contributor_id;
    public List<Contributor> contributors = new ArrayList<>();

    public String getUserIds() {
        return null;
    }
}
