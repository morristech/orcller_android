package com.orcller.app.orcller.model.album;

import java.util.List;

/**
 * Created by pisces on 11/16/15.
 */
public class Pages extends ListEntity {
    public List<Page> data;

    public void deleteHeavyData() {

    }

    public Page getPageAtIndex(int index) {
        if (data == null || index < 0 || index >= data.size())
            return null;
        return data.get(index);
    }
}
