package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.fragment.FindFriendsFragment;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import pisces.psuikit.ext.PSActionBarActivity;

/**
 * Created by pisces on 1/11/16.
 */
public class FindFriendsActivity extends PSActionBarActivity {
    private FindFriendsFragment fragment;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_findfriends);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.w_title_find_friends));

        fragment = (FindFriendsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        fragment.setActionBar(getSupportActionBar());
        fragment.invalidateFragment();
        fragment.setActive(true);
        SoftKeyboardNotifier.getDefault().register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SoftKeyboardNotifier.getDefault().unregister(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    // ================================================================================================
    //  Private
    // ================================================================================================

}
