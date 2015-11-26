package pisces.psfoundation.utils;

import android.text.TextUtils;

import java.io.Serializable;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 11/8/15.
 */
public class Log {
    public static void d(String msg) {
        android.util.Log.d(Application.applicationContext().getPackageName(), msg);
    }

    public static void d(String tag, Object... objects) {
        String[] msgs = new String[objects.length];
        int i = 0;

        for (Object object : objects) {
            String msg = object instanceof Serializable ? GsonUtil.toGsonString(object) : String.valueOf(object);
            msgs[i++] = msg;
        }

        android.util.Log.d(Application.applicationContext().getPackageName(), tag + " -> " + TextUtils.join(", ", msgs));
    }

    public static void e(String msg) {
        android.util.Log.d(Application.applicationContext().getPackageName(), msg);
    }

    public static void e(String tag, Object... objects) {
        String[] msgs = new String[objects.length];
        int i = 0;

        for (Object object : objects) {
            String msg = object instanceof Serializable ? GsonUtil.toGsonString(object) : String.valueOf(object);
            msgs[i++] = msg;
        }

        android.util.Log.d(Application.applicationContext().getPackageName(), tag + " -> " + TextUtils.join(", ", msgs));
    }

    public static void e(String tag, String msg) {
        android.util.Log.i(Application.applicationContext().getPackageName(), tag + " -> " + msg);
    }

    public static void i(String msg) {
        android.util.Log.i(Application.applicationContext().getPackageName(), msg);
    }

    public static void i(String tag, Object... objects) {
        String[] msgs = new String[objects.length];
        int i = 0;

        for (Object object : objects) {
            String msg = object instanceof Serializable ? GsonUtil.toGsonString(object) : String.valueOf(object);
            msgs[i++] = msg;
        }

        android.util.Log.i(Application.applicationContext().getPackageName(), tag + " -> " + TextUtils.join(", ", msgs));
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(Application.applicationContext().getPackageName(), tag + " -> " + msg);
    }
}
