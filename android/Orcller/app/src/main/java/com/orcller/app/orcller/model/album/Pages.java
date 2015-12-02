package com.orcller.app.orcller.model.album;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pisces on 11/16/15.
 */
public class Pages extends ListEntity {
    public ArrayList<Page> data = new ArrayList<>();

    public void deleteHeavyData() {

    }

    public Page getPageAtIndex(int index) {
        if (data == null || index < 0 || index >= data.size())
            return null;
        return data.get(index);
    }
}
