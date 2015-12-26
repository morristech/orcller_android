package com.orcller.app.orcller.model;

/**
 * Created by pisces on 11/16/15.
 */
public class Pages extends ListEntity<Page> {
    public void deleteHeavyData() {
        if (data != null && data.size() > 3) {
            data.subList(3, data.size() - 1).clear();
            count = data.size();
        }
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
