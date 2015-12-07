package com.orcller.app.orcller.model.album;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pisces on 11/16/15.
 */
public class Contributors extends AlbumAdditionalListEntity<Contributor> {
    public int contributor_status;
    public String contributor_id;

    public String getUserIds() {
        return null;
    }
}
