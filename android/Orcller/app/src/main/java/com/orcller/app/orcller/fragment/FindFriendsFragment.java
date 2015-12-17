package com.orcller.app.orcller.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcller.app.orcller.R;

import pisces.psuikit.ext.PSFragment;

/**
 * Created by pisces on 11/3/15.
 */
public class FindFriendsFragment extends MainTabFragment {
    public FindFriendsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_findfrineds, null);
    }
}
