package com.orcller.app.orcller.fragment;

import android.view.View;

import pisces.psuikit.ext.PSFragment;

/**
 * Created by pisces on 12/18/15.
 */
abstract public class MainTabFragment extends PSFragment {
    private boolean shouldStartFragment;

    // ================================================================================================
    //  Overridden: PSFragment
    // ================================================================================================

    protected void commitProperties() {
        if (shouldStartFragment) {
            shouldStartFragment = false;
            startFragment();
        }
    }

    @Override
    protected void setUpSubviews(View view) {
        setHasOptionsMenu(true);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void invalidateFragment() {
        shouldStartFragment = true;

        invalidateProperties();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    abstract protected void startFragment();
}
