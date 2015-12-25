package com.orcller.app.orcller.model;

import java.util.Map;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 12/25/15.
 */
public class PushNotificationObject extends Model {
    public enum Type {
        Application(1<<0),
        Album(1<<1),
        Relationships(1<<2);

        private int value;

        private Type(int value) {
            this.value = value;
        }

        public boolean equals(int value) {
            return this.value == value;
        }

        public int value() {
            return value;
        }
    }

    public int type;
    public String message;
    public String sound;
    public String title;
    public Map<String, Object> data;
}
