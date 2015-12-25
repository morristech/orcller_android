package com.orcller.app.orcller.service;

import com.orcller.app.orcller.facade.ApplicationFacade;

/**
 * Created by pisces on 11/10/15.
 */
public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        ApplicationFacade.getDefault().onTokenRefresh();
    }
}
