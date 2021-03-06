package pisces.psfoundation.ext;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/5/15.
 */
public class Application extends android.app.Application {
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    private static Context context;
    private static Activity topActivity;

    // ================================================================================================
    //  Overridden: android.app.Application
    // ================================================================================================

    @Override
    public void onCreate(){
        super.onCreate();

        context = getApplicationContext();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static Context applicationContext() {
        return context;
    }

    public static int compareVersions(String v1, String v2) {
        if (v1.length() > 0  && v2.length() == 0) return -1;
        if (v1.length() == 0 && v2.length() == 0) return 0;
        if (v1.length() == 0 && v2.length() < 0) return 1;

        int pos1 = v1.indexOf('.');
        int pos2 = v2.indexOf('.');

        Integer num1 = (pos1 > 0 ? Integer.valueOf(v1.substring(0, pos1)) : 0);
        Integer num2 = (pos2 > 0 ? Integer.valueOf(v2.substring(0, pos2)) : 0);

        if (num1 != num2) return num1.compareTo(num2);

        String tail1 = (pos1 > 0 ? v1.substring(pos1 + 1, v1.length()) : "");
        String tail2 = (pos2 > 0 ? v2.substring(pos2 + 1, v2.length()) : "");

        return compareVersions(tail1, tail2);
    }

    public static ConnectivityManager getConnectivityManager() {
        return  (ConnectivityManager) getTopActivity().getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static int getPackageVersionCode() {
        return getPackageInfo().versionCode;
    }

    public static String getPackageVersionName() {
        return getPackageInfo().versionName;
    }

    public static Activity getTopActivity() {
        return topActivity;
    }

    public static void setTopActivity(Activity topActivity) {
        Application.topActivity = topActivity;
    }

    public static int getWindowHeight() {
        return getTopActivity().getWindow().getDecorView().getHeight();
    }

    public static int getWindowWidth() {
        return getTopActivity().getWindow().getDecorView().getWidth();
    }

    public static boolean equalsAppVersion(String version) {
        return compareVersions(getPackageVersionName(), version) == 0;
    }

    public static boolean isHigherAppVersion(String version) {
        return compareVersions(getPackageVersionName(), version) == 1;
    }

    public static boolean isInBackground() {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public static boolean isLowerAppVersion(String version) {
        return compareVersions(getPackageVersionName(), version) == -1;
    }

    public static boolean isNetworkConnected() {
        ConnectivityManager manager = getConnectivityManager();
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mobile.isConnected() || wifi.isConnected();
    }

    public static boolean isNotificationEnabled() {
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        Class appOpsClass;

        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int)opPostNotificationValue.get(Integer.class);
            return ((int)checkOpNoThrowMethod.invoke(mAppOps,value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void moveToBack(Activity activity) {
        if (activity == null || getTopActivity().equals(activity))
            return;

        Intent intent = activity.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
    }

    public static AsyncTask run(final Runnable doInBackground, final Runnable onPostExecute) {
        return new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (doInBackground != null)
                    doInBackground.run();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                if (onPostExecute != null)
                    onPostExecute.run();
            }
        }.execute();
    }

    public static Thread runOnBackgroundThread(Runnable runnable) {
        Thread thread = new Thread(runnable, "Background");
        thread.start();
        return thread;
    }

    public static void runOnMainThread(Runnable runnable) {
        new Handler(applicationContext().getMainLooper()).post(runnable);
    }

    public static void setBadge(Context context, int count) {
        setBadgeSamsung(context, count);
        setBadgeSony(context, count);
    }

    public static void clearBadge(Context context) {
        setBadgeSamsung(context, 0);
        clearBadgeSony(context);
    }

    public static void startActivity(Class clazz) {
        Intent intent = new Intent(topActivity, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        topActivity.startActivity(intent);
    }

    public static void startActivity(Intent intent, int enterAnim, int exitAnim) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        topActivity.startActivity(intent);
        topActivity.overridePendingTransition(enterAnim, exitAnim);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private static PackageInfo getPackageInfo() {
        try {
            return applicationContext()
                    .getPackageManager()
                    .getPackageInfo(applicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static void setBadgeSamsung(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    private static void setBadgeSony(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", launcherClassName);
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", true);
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(count));
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());

        context.sendBroadcast(intent);
    }

    private static void clearBadgeSony(Context context) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", launcherClassName);
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", false);
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(0));
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());

        context.sendBroadcast(intent);
    }

    private static String getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }
}
