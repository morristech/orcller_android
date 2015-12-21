package com.orcller.app.orcller.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import pisces.psuikit.ext.PSFragment;

/**
 * Created by pisces on 12/18/15.
 */
abstract public class MainTabFragment extends PSFragment {
    private boolean initialized;
    private boolean isFirstLoading = true;
    private boolean shouldStartFragment;
    private boolean viewCreated;

    // ================================================================================================
    //  Overridden: PSFragment
    // ================================================================================================

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialized = true;
        viewCreated = true;

        setHasOptionsMenu(true);
        setUpViews(view);
        validateFragment();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public String getToolbarTitle() {
        return null;
    }

    public void invalidateFragment() {
        shouldStartFragment = true;

        if (initialized)
            validateFragment();
    }

    public boolean isUseSoftKeyboard() {
        return false;
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    abstract protected void setUpViews(View view);

    protected boolean isViewCreated() {
        return viewCreated;
    }

    protected void resumeFragment() {
    }

    protected void startFragment() {
    }

    private void validateFragment() {
        if (!shouldStartFragment)
            return;

        if (isFirstLoading) {
            startFragment();
            isFirstLoading = false;
        } else {
            resumeFragment();
        }

        shouldStartFragment = false;
    }
}
