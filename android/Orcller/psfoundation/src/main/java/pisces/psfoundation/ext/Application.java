package pisces.psfoundation.ext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;

import java.util.List;

/**
 * Created by pisces on 11/5/15.
 */
public class Application extends android.app.Application {
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

    public static int compareVersions(String left, String right) {
        if (left.equals(right))
            return 0;

        int leftStart = 0, rightStart = 0, result;
        do {
            int leftEnd = left.indexOf('.', leftStart);
            int rightEnd = right.indexOf('.', rightStart);
            Integer leftValue = Integer.parseInt(leftEnd < 0
                    ? left.substring(leftStart)
                    : left.substring(leftStart, leftEnd));
            Integer rightValue = Integer.parseInt(rightEnd < 0
                    ? right.substring(rightStart)
                    : right.substring(rightStart, rightEnd));
            result = leftValue.compareTo(rightValue);
            leftStart = leftEnd;
            rightStart = rightEnd;
        } while (result == 0 && leftStart > 0 && rightStart > 0);
        if (result == 0) {
            if (leftStart > rightStart) {
                return 1;
            }
            if (leftStart < rightStart) {
                return -1;
            }
        }
        return result;
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

    public static boolean isNetworkConnected() {
        ConnectivityManager manager = getConnectivityManager();
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mobile.isConnected() || wifi.isConnected();
    }

    public static boolean isHigherAppVersion(String version) {
        return compareVersions(getPackageVersionName(), version) == 1;
    }

    public static boolean isLowerAppVersion(String version) {
        return compareVersions(getPackageVersionName(), version) == -1;
    }

    public static void moveToBack(Activity activity) {
        if (getTopActivity().equals(activity))
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
