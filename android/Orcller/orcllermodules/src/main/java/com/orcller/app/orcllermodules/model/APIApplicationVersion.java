package com.orcller.app.orcllermodules.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pisces on 11/5/15.
 */
public class APIApplicationVersion extends APIResult {
    @SerializedName("entity")
    public APIApplicationVersion.Entity entity;

    public class Entity {
        public boolean is_release;
        public String version;
    }
}
