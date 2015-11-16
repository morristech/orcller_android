package com.orcller.app.orcllermodules.model.facebook;


import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/12/15.
 */
public class FBUser extends Model {
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

    public static class Picture extends Model {
        public Data data;
    }

    public static class Data extends Model {
        public boolean is_silhouette;
        public int height;
        public int width;
        public String url;
    }
}
