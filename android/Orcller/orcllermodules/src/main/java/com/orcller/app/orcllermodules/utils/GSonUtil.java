package com.orcller.app.orcllermodules.utils;

import com.google.gson.Gson;

/**
 * Created by pisces on 11/4/15.
 */
public class GSonUtil {
    public static String toGSonString(Object object) {
        return new Gson().toJson(object);
    }
    public static Object objectFromGSonString(String json, Class classType) {
        return  new Gson().fromJson(json, classType);
    }
}
