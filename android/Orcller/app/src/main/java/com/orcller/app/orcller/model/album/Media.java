package com.orcller.app.orcller.model.album;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/16/15.
 */
public class Media extends Model {
    public enum Type {
        Image(1),
        Video(2);

        private int value;

        private Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    };

    public enum OriginType {
        Facebook(1),
        Instagram(2),
        Local(3);

        private int value;

        private OriginType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public long id;
    public long origin_id;
    public Type type;
    public OriginType origin_type;
    public Images images;

    public boolean isVideo() {
        return type.equals(Type.Video);
    }
}
