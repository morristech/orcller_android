package com.orcller.app.orcller.fragment;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.orcller.app.orcller.AnalyticsTrackers;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSFragment;

/**
 * Created by pisces on 12/18/15.
 */
abstract public class MainTabFragment extends PSFragment {
    private boolean shouldStartFragment;
    private ActionBar actionBar;

    // ================================================================================================
    //  Overridden: PSFragment
    // ================================================================================================

    protected void commitProperties() {
        if (shouldStartFragment) {
            shouldStartFragment = false;
            startFragment();
            trackFragment();
        }
    }

    @Override
    protected void setUpSubviews(View view) {
        setHasOptionsMenu(true);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public ActionBar getActionBar() {
        return actionBar;
    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
    }

    public void invalidateFragment() {
        shouldStartFragment = true;

        invalidateProperties();
    }

    public void scrollToTop() {
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    abstract protected void startFragment();

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void trackFragment() {
        AnalyticsTrackers.getInstance().trackScreen(AnalyticsTrackers.Target.APP, getClass().getName());
    }
}