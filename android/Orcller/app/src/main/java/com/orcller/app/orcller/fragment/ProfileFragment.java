package com.orcller.app.orcller.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcller.app.orcller.R;

/**
 * Created by pisces on 11/3/15.
 */
@SuppressLint("ValidFragment")
public class ProfileFragment extends Fragment {
    Context mContext;

    public ProfileFragment() {
    }

    public ProfileFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, null);


        return view;
    }
}
