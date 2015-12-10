package com.orcller.app.orcller.utils;

import android.text.Html;
import android.text.Spannable;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.model.BaseUser;
import com.orcller.app.orcllermodules.model.User;

import java.util.HashMap;
import java.util.Map;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.HtmlUtils;
import pisces.psfoundation.utils.MapUtils;

/**
 * Created by pisces on 12/7/15.
 */
public class CustomSchemeGenerator {
    public static final String SCHEMA = Application.applicationContext().getString(R.string.app_scheme);

    public enum Category {
        Album(Application.applicationContext().getString(R.string.host_album)),
        Coediting(Application.applicationContext().getString(R.string.host_coediting)),
        Feed(Application.applicationContext().getString(R.string.host_feed)),
        Member(Application.applicationContext().getString(R.string.host_member)),
        Notification(Application.applicationContext().getString(R.string.host_notification)),
        Options(Application.applicationContext().getString(R.string.host_options)),
        Relationships(Application.applicationContext().getString(R.string.host_relationships)),
        Users(Application.applicationContext().getString(R.string.host_users));

        private String value;

        private Category(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ViewTypeAlbum {
        View(1),
        Create(2),
        Modify(3);

        private int value;

        private ViewTypeAlbum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ViewTypeCoediting {
        View(1);

        private int value;

        private ViewTypeCoediting(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ViewTypeFeed {
        View(1);

        private int value;

        private ViewTypeFeed(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ViewTypeMember {
        JoinInputView(1);

        private int value;

        private ViewTypeMember(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ViewTypeNotification {
        View(1);

        private int value;

        private ViewTypeNotification(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ViewTypeOptions {
        View(1),
        PrivatePolicy(2),
        Terms(3);

        private int value;

        private ViewTypeOptions(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ViewTypeRelationships {
        Recommend(1),
        Follower(2),
        Following(3);

        private int value;

        private ViewTypeRelationships(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ViewTypeUsers {
        Profile(1),
        UserPicture(2);

        private int value;

        private ViewTypeUsers(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static String create(Category category, int viewType, Map<String, String> param) {
        return SCHEMA + "://" + category.getValue() + "/" + String.valueOf(viewType) + "?" + MapUtils.toQueryString(param);
    }

    public static CharSequence createUserProfileHtml(BaseUser user) {
        String tag = "<a href=\"" + createUserProfile(user) + "\" >" + user.user_id + "</a>";
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(
                Html.fromHtml(tag));
        return HtmlUtils.removeUnderlines(spannable);
    }

    public static String createUserProfile(long userId) {
        Map<String, String> param = new HashMap<>();
        param.put("user_uid", String.valueOf(userId));
        return create(Category.Users, ViewTypeUsers.Profile.getValue(), param);
    }

    public static String createUserProfile(BaseUser user) {
        return createUserProfile(user.user_uid);
    }
}
