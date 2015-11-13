package com.orcller.app.orcller.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;

import pisces.psuikit.ext.PSFragment;

/**
 * Created by pisces on 11/3/15.
 */
@SuppressLint("ValidFragment")
public class TimelineFragment extends PSFragment {
    public TimelineFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button button = (Button) getView().findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthenticationCenter.getDefault().logout(null);
            }
        });
    }
}
