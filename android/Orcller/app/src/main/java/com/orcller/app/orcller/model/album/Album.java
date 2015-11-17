package com.orcller.app.orcller.model.album;

import android.text.TextUtils;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 11/15/15.
 */
public class Album extends AlbumInfo {
    public enum Permission {
        Public(1),
        Followers(2),
        Private(3);

        private int value;

        private Permission(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ReportType {
        Inappropriate(1);

        private int value;

        private ReportType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public boolean page_replace_enabled;
    public int default_page_index;
    public int permission;
    public long updated_time;
    public String desc;
    public String encrypted_id;
    public String name;
    public Contributors contributors;
    public Favorites favorites;
    public Likes likes;
    public Pages pages;

    public boolean addPage(Page page) {
        if (page == null || pages.data.contains(page))
            return false;

        page.order = pages.total_count + 1;
        pages.data.add(page);
        pages.count = pages.total_count = pages.data.size();

        Collections.sort(pages.data, new Comparator<Page>() {
            @Override
            public int compare(Page lhs, Page rhs) {
                if (lhs.order < rhs.order)
                    return 1;
                if (lhs.order > rhs.order)
                    return -1;
                return 0;
            }
        });

        return true;
    }

    public Page getCover() {
        return pages.data.size() > 0 ? pages.data.get(0) : null;
    }

    public String getViewName() {
        return TextUtils.isEmpty(name) ?
                Application.applicationContext().getResources().getString(R.string.w_untitled) :
                name;
    }

    public boolean insertPage(Page page, int index) {
        if (page == null || pages.data.contains(page))
            return false;

        pages.data.add(index, page);
        pages.count = pages.total_count = pages.data.size();

        return true;
    }

    public boolean isMine() {
        if (AuthenticationCenter.getDefault().getUser() == null)
            return false;
        return AuthenticationCenter.getDefault().getUser().user_uid == user_uid;
    }

    public void removeAllPages() {
        pages.data.clear();
        pages.count = pages.total_count = pages.data.size();
    }

    public boolean removePage(Page page) {
        return pages.data.remove(page);
    }

    public boolean removePage(int index) {
        if (index < 0 || index >= pages.data.size())
            return false;
        return pages.data.remove(index) != null;
    }
}