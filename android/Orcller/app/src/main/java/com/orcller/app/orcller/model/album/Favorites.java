package com.orcller.app.orcller.model.album;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pisces on 11/16/15.
 */
public class Favorites extends ListEntity {
    public boolean participated;
    public long id;
    public List<Favorite> data = new ArrayList<>();
}
