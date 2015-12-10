package com.orcller.app.orcller.activity;

import android.os.Bundle;

import com.orcller.app.orcller.R;

import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 12/10/15.
 */
public class ProfileActivity extends PSActionBarActivity {

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide(this);
    }
}
