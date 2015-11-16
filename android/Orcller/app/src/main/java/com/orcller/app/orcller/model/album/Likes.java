package com.orcller.app.orcller.model.album;

import java.util.List;

/**
 * Created by pisces on 11/16/15.
 */
public class Likes extends ListEntity {
    public boolean participated;
    public long id;
    public List<Like> data;
}
