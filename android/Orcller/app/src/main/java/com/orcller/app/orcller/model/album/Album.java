package com.orcller.app.orcller.model.album;

import android.text.TextUtils;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.DateUtil;

/**
 * Created by pisces on 11/15/15.
 */
public class Album extends AlbumInfo {
    public enum Permission {
        None(0),
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
    public String desc;
    public String encrypted_id;
    public String name;
    public Comments comments = new Comments();
    public Contributors contributors = new Contributors();
    public Favorites favorites = new Favorites();
    public Likes likes = new Likes();
    public Pages pages = new Pages();

    public Album(User user) {
        super();

        id = DateUtil.toUnixtimestamp(new Date());

        if (user != null) {
            permission = user.user_options.album_permission;
            user_uid = user.user_uid;
            user_id = user.user_id;
            user_link = user.user_link;
            user_name = user.user_name;
            user_picture = user.user_picture;
        }

        Locale locale = Application.applicationContext().getResources().getConfiguration().locale;
        Pattern pattern = Pattern.compile("(.*)ko(.*)");
        String formatString = "MMM d, yyyy aaa";

        if (pattern.matcher(locale.toLanguageTag()).matches())
            formatString = "yyyy년 M월 d일 aaa";

        name = new SimpleDateFormat(formatString).format(new Date());
    }

    public boolean addPage(Page page) {
        if (page == null || pages.data.contains(page))
            return false;

        page.order = pages.total_count + 1;
        pages.data.add(page);
        pages.count = pages.total_count = pages.data.size();

        Collections.sort(pages.data, new Comparator<Page>() {
            @Override
            public int compare(Page lhs, Page rhs) {
                if (lhs.order > rhs.order)
                    return 1;
                if (lhs.order < rhs.order)
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