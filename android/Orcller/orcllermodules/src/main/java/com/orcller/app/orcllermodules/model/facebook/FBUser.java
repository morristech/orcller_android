package com.orcller.app.orcllermodules.model.facebook;

import com.orcller.app.orcllermodules.model.AbstractModel;

import java.util.Date;

/**
 * Created by pisces on 11/12/15.
 */
public class FBUser extends AbstractModel {
    public int timezone;
    public boolean verified;
    public String first_name;
    public String gender;
    public String id;
    public String last_name;
    public String link;
    public String locale;
    public String name;
    public String updated_time;
    public Picture picture;

    public class Picture {
        public Data data;
    }

    public class Data {
        boolean is_silhouette;
        int height;
        int width;
        public String url;
    }
}
