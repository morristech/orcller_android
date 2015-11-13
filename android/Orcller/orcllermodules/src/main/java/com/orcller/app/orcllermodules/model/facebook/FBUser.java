package com.orcller.app.orcllermodules.model.facebook;


import pisces.psfoundation.model.AbstractModel;

/**
 * Created by pisces on 11/12/15.
 */
public class FBUser extends AbstractModel {
    public boolean verified;
    public int timezone;
    public String first_name;
    public String gender;
    public String id;
    public String last_name;
    public String link;
    public String locale;
    public String name;
    public String updated_time;
    public Picture picture;

    public static class Picture extends AbstractModel {
        public Data data;
    }

    public static class Data extends AbstractModel {
        public boolean is_silhouette;
        public int height;
        public int width;
        public String url;
    }
}
