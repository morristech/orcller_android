package com.orcller.app.orcller.model;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by pisces on 11/16/15.
 */
public class Pages extends ListEntity<Page> {
    public void deleteHeavyData() {
        if (data != null && data.size() > 3) {
            data.subList(3, data.size()).clear();
            count = data.size();
        }
    }

    public Page getFirstPage() {
        return data != null && data.size() > 0 ? data.get(0) : null;
    }

    public Page getPageAtIndex(int index) {
        if (data == null || index < 0 || index >= data.size())
            return null;
        return data.get(index);
    }

    public Page getPageById(long id) {
        for (Page page : data) {
            if (page.id == id) {
                return page;
            }
        }
        return null;
    }

    public int getPageIndex(Page page) {
        return data.indexOf(page);
    }

    public Page getLastPage() {
        return data != null && data.size() == total_count ? data.get(total_count - 1) : null;
    }

    public boolean addPage(Page page, boolean sortable) {
        if (page == null || data.contains(page))
            return false;

        page.order = total_count + 1;
        data.add(page);
        count = total_count = data.size();

        if (sortable)
            sortByOrder(true);

        return true;
    }

    public boolean insertPage(Page page, int index) {
        if (page == null || data.contains(page))
            return false;

        data.add(index, page);
        count = total_count = data.size();

        return true;
    }

    public void removeAllPages() {
        data.clear();
        count = total_count = data.size();
    }

    public boolean removePage(Page page) {
        boolean result = data.remove(page);
        count = total_count = data.size();
        return result;
    }

    public boolean removePage(int index) {
        if (index < 0 || index >= data.size())
            return false;

        boolean result = data.remove(index) != null;
        count = total_count = data.size();
        return result;
    }

    public boolean removePageById(long id) {
        for (Page page : data) {
            if (page.id == id) {
                removePage(page);
                count = total_count = data.size();
                return true;
            }
        }
        return false;
    }

    public void sortByOrder(boolean ascending) {
        if (data == null)
            return;

        final int left = ascending ? 1 : -1;
        final int right = ascending ? -1 : 1;

        Collections.sort(data, new Comparator<Page>() {
            @Override
            public int compare(Page lhs, Page rhs) {
                if (lhs.order > rhs.order)
                    return left;
                if (lhs.order < rhs.order)
                    return right;
                return 0;
            }
        });
    }
}
