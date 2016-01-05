package com.orcller.app.orcller.fragment;

import android.support.v7.app.ActionBar;
import android.view.View;

import com.orcller.app.orcller.AnalyticsTrackers;

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

    @Override
    public void onResume() {
        super.onResume();

        setActive(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        setActive(false);
    }

    @Override
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