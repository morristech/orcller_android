package com.orcller.app.orcllermodules.utils;

import com.google.gson.Gson;

import java.io.StringWriter;

/**
 * Created by pisces on 11/4/15.
 */
public class GSonUtil {
    public static String toGSonString(Object object) {
        return new Gson().toJson(object);
    }
}
