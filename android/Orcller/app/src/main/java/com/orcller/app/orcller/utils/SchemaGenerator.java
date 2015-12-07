package com.orcller.app.orcller.utils;

import android.text.Html;
import android.text.Spannable;

import com.orcller.app.orcllermodules.model.User;

import java.util.HashMap;
import java.util.Map;

import pisces.psfoundation.utils.HtmlUtils;
import pisces.psfoundation.utils.MapUtils;

/**
 * Created by pisces on 12/7/15.
 */
public class SchemaGenerator {
    public static final String SCHEMA = "orcller";

    public enum Category {
        Album("album"),
        Coediting("coediting"),
        Feed("feed"),
        Member("member"),
        Notification("notification"),
        Options("options"),
        Relationships("relationships"),
        Users("users");

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

    public static CharSequence createHtmlForUserProfile(User user) {
        Map<String, String> param = new HashMap<>();
        param.put("user_uid", String.valueOf(user.user_uid));
        String link = createSchema(Category.Users, ViewTypeUsers.Profile.getValue(), param);
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(
                Html.fromHtml("<a href=\"" + link + "\" >" + user.user_id + "</a>"));
        return HtmlUtils.removeUnderlines(spannable);
    }

    public static String createSchema(Category category, int viewType, Map<String, String> param) {
        return SCHEMA + "//:" + category.getValue() + "/" + String.valueOf(viewType) + "?" + MapUtils.toQueryString(param);
    }
}
