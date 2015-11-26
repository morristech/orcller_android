package com.orcller.app.orcllermodules.model.facebook;

import java.lang.reflect.Type;
import java.util.List;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/26/15.
 */
public class FBMediaList<T extends Model> extends Model {
    public Paging paging;

    public List<T> getData() {
        return null;
    }

    public static class Paging {
        public String next;
        public Cursors cursors;

        public boolean hasNext() {
            return next != null;
        }
    }

    public static class Cursors {
        public String before;
        public String after;
    }
}
