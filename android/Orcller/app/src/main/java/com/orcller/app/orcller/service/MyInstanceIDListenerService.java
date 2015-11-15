package com.orcller.app.orcller.service;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/10/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Log.i("onTokenRefresh");
        startService(new Intent(this, ApplicationService.class));
    }
}
