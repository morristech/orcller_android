package com.orcller.app.orcller.activity;

import android.os.Bundle;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.facade.ApplicationFacade;

import pisces.psuikit.ext.PSActivity;

public class SplashActivity extends PSActivity {

    // ================================================================================================
    //  Overridden: PSActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        ApplicationFacade.getDefault().run(this, getIntent());
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
