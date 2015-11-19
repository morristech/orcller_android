package pisces.psfoundation.ext;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

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

    public static boolean isEquals(String version) {
        return compareVersions(getPackageVersionName(), version) == 0;
    }

    public static boolean isHigher(String version) {
        return compareVersions(getPackageVersionName(), version) == 1;
    }

    public static boolean isLower(String version) {
        return compareVersions(getPackageVersionName(), version) == -1;
    }

    public static Thread runOnBackgroundThread(Runnable runnable) {
        Thread thread = new Thread(runnable, "Background");
        thread.start();
        return thread;
    }

    public static void runOnMainThread(Runnable runnable) {
        new Handler(applicationContext().getMainLooper()).post(runnable);
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
}