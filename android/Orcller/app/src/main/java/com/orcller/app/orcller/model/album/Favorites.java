package com.orcller.app.orcller.model.album;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pisces on 11/16/15.
 */
public class Favorites extends ListEntity {
    public int participated;
    public long id;
    public List<Favorite> data = new ArrayList<>();

    public boolean getParticipated() {
        return participated > 0;
    }
}
