package com.orcller.app.orcller.model.album;

import android.text.TextUtils;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.model.User;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 12/18/15.
 */
public class Notification extends Model {
    public enum Type {
        AlbumCreation(1),
        AlbumModification(2),
        Comment(3),
        Like(4),
        Favorite(5),
        CoeditingAsk(6),
        CoeditingAccept(7),
        CoeditingInvite(8),
        Follow(9),
        PageComment(10),
        PageLike(11),
        CoeditingCancelAsk(12),
        CoeditingCancelInvite(13);

        private int value;

        private Type(int value) {
            this.value = value;
        }

        public boolean equals(int value) {
            return value == this.value;
        }

        public final int value() {
            return value;
        }
    }

    public long content_id;
    public long id;
    public int type;
    public long created_time;
    public Content content;
    public Senders senders;

    public static class Content {
        public long content_user_uid;
        public String content_name;
        public String content_thumbnail_url;

        public String getContentViewName() {
            return TextUtils.isEmpty(content_name) ?
                    Application.getTopActivity().getString(R.string.w_untitled) :
                    content_name;
        }
    }

    public static class Senders extends ListEntity<User> {
        public User getFirstUser() {
            return data.get(0);
        }
    }
}
