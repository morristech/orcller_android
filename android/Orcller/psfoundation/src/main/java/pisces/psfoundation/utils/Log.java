package pisces.psfoundation.utils;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 11/8/15.
 */
public class Log {
    public static void i(String msg) {
        android.util.Log.i(Application.applicationContext().getPackageName(), msg);
    }

    public static void i(String tag, Object object) {
        android.util.Log.i(Application.applicationContext().getPackageName(), tag + " -> " + String.valueOf(object));
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(Application.applicationContext().getPackageName(), tag + " -> " + msg);
    }
}
