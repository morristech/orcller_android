package com.orcller.app.orcller.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.managers.DeviceManager;
import com.orcller.app.orcllermodules.utils.Log;

import java.io.IOException;

/**
 * Created by pisces on 11/10/15.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegistrationIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            synchronized (TAG) {
                String default_senderId = getString(R.string.gcm_defaultSenderId);
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                token = instanceID.getToken(default_senderId, scope, null);
                DeviceManager.getDefault().registerDeviceToken(token);

                Log.i("token", token);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}