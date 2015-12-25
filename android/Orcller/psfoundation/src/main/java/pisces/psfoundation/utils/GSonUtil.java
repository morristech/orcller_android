package pisces.psfoundation.utils;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Created by pisces on 11/4/15.
 */
public class GsonUtil {
    public static String toGsonString(Object object) {
        return new Gson().toJson(object);
    }

    public static Map<String, String> toMap(Object object) {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return (Map<String, String>) fromJson(toGsonString(object), type);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    public static <T> T fromJson(JsonElement json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    public static Object fromJson(String json, Type type) {
        return new Gson().fromJson(json, type);
    }
}
