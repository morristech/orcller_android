package com.orcller.app.orcller.service;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by pisces on 11/10/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        startService(new Intent(this, ApplicationService.class));
    }
}
