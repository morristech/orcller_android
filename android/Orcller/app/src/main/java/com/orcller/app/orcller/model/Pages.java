package com.orcller.app.orcller.model;

/**
 * Created by pisces on 11/16/15.
 */
public class Pages extends ListEntity<Page> {
    public void deleteHeavyData() {

    }

    public Page getPageAtIndex(int index) {
        if (data == null || index < 0 || index >= data.size())
            return null;
        return data.get(index);
    }

    public int getPageIndex(Page page) {
        return data.indexOf(page);
    }
}