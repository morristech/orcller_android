package com.orcller.app.orcller.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import pisces.psuikit.ext.PSFragment;

/**
 * Created by pisces on 12/18/15.
 */
public class MainTabFragment extends PSFragment {
    private boolean isFirstLoading = true;
    private boolean viewCreated;

    // ================================================================================================
    //  Overridden: PSFragment
    // ================================================================================================

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        viewCreated = true;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void invalidateFragment() {
        if (isFirstLoading) {
            startFragment();
            isFirstLoading = false;
        } else {
            resumeFragment();
        }

        viewCreated = false;
    }

    public boolean isUseSoftKeyboard() {
        return false;
    }

    public String getToolbarTitle() {
        return null;
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected boolean isViewCreated() {
        return viewCreated;
    }

    protected void resumeFragment() {
    }

    protected void startFragment() {
    }
}
