package com.orcller.app.orcllermodules.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by pisces on 11/4/15.
 */
public class GSonUtil {
    public static String toGSonString(Object object) {
        return new Gson().toJson(object);
    }
    public static Map<String, String> toMap(Object object) {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return (Map<String, String>) objectFromGSonString(toGSonString(object), type);
    }
    public static Object objectFromGSonString(String json, Class classType) {
        return  new Gson().fromJson(json, classType);
    }
    public static Object objectFromGSonString(String json, Type type) {
        return  new Gson().fromJson(json, type);
    }
}
