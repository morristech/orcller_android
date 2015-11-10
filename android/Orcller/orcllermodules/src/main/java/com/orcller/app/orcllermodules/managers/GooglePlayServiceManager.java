package com.orcller.app.orcllermodules.managers;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.orcller.app.orcllermodules.ext.Application;
import com.orcller.app.orcllermodules.utils.Log;

/**
 * Created by pisces on 11/11/15.
 */
public class GooglePlayServiceManager {
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static GooglePlayServiceManager uniqueInstance;

    // ================================================================================================
    //  Public
    // ================================================================================================

    /**
     * @constructor
     **/
    public GooglePlayServiceManager() {
    }

    public static GooglePlayServiceManager getDefault() {
        if(uniqueInstance == null) {
            synchronized(GooglePlayServiceManager.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new GooglePlayServiceManager();
                }
            }
        }
        return uniqueInstance;
    }

    public boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("This device is not supported.");
            }
            return false;
        }
        return true;
    }
}
