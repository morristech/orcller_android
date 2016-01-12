package com.orcller.app.orcller.activity;

import android.os.Bundle;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.facade.ApplicationFacade;
import com.orcller.app.orcller.model.PushNotificationObject;
import com.orcller.app.orcller.service.GcmListenerService;

import pisces.psuikit.ext.PSActivity;

public class SplashActivity extends PSActivity {
    private PushNotificationObject data;

    // ================================================================================================
    //  Overridden: Activity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        ApplicationFacade.getDefault().run(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
