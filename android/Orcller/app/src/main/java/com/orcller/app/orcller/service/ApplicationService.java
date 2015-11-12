package com.orcller.app.orcller.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.managers.DeviceManager;
import com.orcller.app.orcllermodules.managers.GooglePlayServiceManager;

import java.io.IOException;

/**
 * Created by pisces on 11/11/15.
 */
public class ApplicationService extends IntentService {
    private static final String TAG = "ApplicationService";

    public ApplicationService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {
        if (!GooglePlayServiceManager.getDefault().checkPlayServices(this))
            return;

        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            synchronized (TAG) {
                String default_senderId = getString(R.string.gcm_defaultSenderId);
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                token = instanceID.getToken(default_senderId, scope, null);
                DeviceManager.getDefault().registerDeviceToken(token);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
