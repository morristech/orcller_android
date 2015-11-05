package com.orcller.app.orcllermodules.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pisces on 11/5/15.
 */
public class ApiApplication {
    public class Version extends APIResult {
        @SerializedName("entity")
        public Version.Entity entity;

        public class Entity {
            public boolean is_release;
            public String version;
            public String version_description;
        }
    }
}
