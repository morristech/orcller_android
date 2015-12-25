package pisces.psuikit.manager;

import android.app.Activity;

import java.util.HashMap;
import java.util.Map;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 12/25/15.
 */
public class ActivityManager {
    private static Map<String, Activity> runningActivityMap = new HashMap<>();

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void clear() {
        runningActivityMap.clear();
    }

    public static Activity getRunningActivity(Class clazz) {
        return runningActivityMap.get(clazz.getName());
    }

    public static boolean hasRunningActivity(Class clazz) {
        return getRunningActivity(clazz) != null;
    }

    public static void putRunningActivity(Activity activity) {
        runningActivityMap.put(activity.getClass().getName(), activity);
        Application.setTopActivity(activity);
    }

    public static void removeRunningActivity(Activity activity) {
        runningActivityMap.remove(activity.getClass().getName());
    }
}
