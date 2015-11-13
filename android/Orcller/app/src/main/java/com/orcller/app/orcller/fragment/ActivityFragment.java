package com.orcller.app.orcller.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcller.app.orcller.R;
import pisces.psuikit.ext.PSFragment;

/**
 * Created by pisces on 11/3/15.
 */
public class ActivityFragment extends PSFragment {
    public ActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity, null);
    }
}
