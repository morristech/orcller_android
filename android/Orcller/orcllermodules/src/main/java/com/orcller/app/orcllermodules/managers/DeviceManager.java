package com.orcller.app.orcllermodules.managers;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 11/10/15.
 */
public class DeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();
    public static final String SYSTEM_NAME = "Android OS";
    private static DeviceManager uniqueInstance;
    private String deviceToken;

    // ================================================================================================
    //  Public
    // ================================================================================================

    /**
     * @constructor
     **/
    public DeviceManager() {
    }

    public static DeviceManager getDefault() {
        if(uniqueInstance == null) {
            synchronized(DeviceManager.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new DeviceManager();
                }
            }
        }
        return uniqueInstance;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    private String getToken(String gcmDefaultSenderId) {
        Context context = Application.applicationContext();
        if (GooglePlayServiceManager.getDefault().checkPlayServices(context)) {
            InstanceID instanceID = InstanceID.getInstance(context);
            try {
                synchronized (TAG) {
                    String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                    return instanceID.getToken(gcmDefaultSenderId, scope, null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void registerDeviceToken(final String gcmDefaultSenderId) {
        registerDeviceToken(gcmDefaultSenderId, null);
    }
    public void registerDeviceToken(final String gcmDefaultSenderId, Runnable completion) {
        registerDeviceToken(gcmDefaultSenderId, false, completion);
    }
    public void registerDeviceToken(final String gcmDefaultSenderId, final boolean refresh) {
        registerDeviceToken(gcmDefaultSenderId, refresh, null);
    }
    public void registerDeviceToken(final String gcmDefaultSenderId, final boolean refresh, final Runnable completion) {
        Application.run(new Runnable() {
            @Override
            public void run() {
                deviceToken = getToken(gcmDefaultSenderId);
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (completion != null)
                    completion.run();

                if (refresh && deviceToken != null)
                    AuthenticationCenter.getDefault().updateDevice();
            }
        });
    }
}
