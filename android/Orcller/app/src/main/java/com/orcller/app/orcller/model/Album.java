package com.orcller.app.orcller.model;

import android.text.TextUtils;

import com.orcller.app.orcller.R;
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

        public int value() {
            return value;
        }
    }

    public enum ReportType {
        Inappropriate(1);

        private int value;

        private ReportType(int value) {
            this.value = value;
        }

        public int value() {
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

    public Album() {
        super();

        init(null);
    }

    public Album(User user) {
        super();

        init(user);
    }

    public Page getCover() {
        return pages.data.size() > 0 ? pages.data.get(0) : null;
    }

    public String getViewName() {
        return TextUtils.isEmpty(name) ?
                Application.applicationContext().getResources().getString(R.string.w_untitled) :
                name;
    }

    private void init(User user) {
        Locale locale = Application.applicationContext().getResources().getConfiguration().locale;
        Pattern pattern = Pattern.compile("(.*)ko(.*)");
        String formatString = "MMM d, yyyy aaa";

        if (pattern.matcher(locale.getLanguage()).matches())
            formatString = "yyyy년 M월 d일 aaa";

        name = new SimpleDateFormat(formatString).format(new Date());
        id = DateUtil.toUnixtimestamp(new Date());

        if (user != null) {
            this.user = user;
            permission = user.user_options.album_permission;
            user_uid = user.user_uid;
            user_id = user.user_id;
            user_link = user.user_link;
            user_name = user.user_name;
            user_picture = user.user_picture;
        }
    }
}