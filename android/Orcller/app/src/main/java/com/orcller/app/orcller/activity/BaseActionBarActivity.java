package com.orcller.app.orcller.activity;

import android.os.Bundle;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;

import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ActivityManager;
import pisces.psuikit.manager.ExceptionViewManager;

/**
 * Created by pisces on 1/21/16.
 */
public class BaseActionBarActivity extends PSActionBarActivity {
    private GoogleApiClient client;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        Action action = createAction();
        if (action != null) {
            AppIndex.AppIndexApi.start(client, action);
            client.connect();
        }
    }

    public void onStop() {
        super.onStop();

        Action action = createAction();
        if (action != null) {
            AppIndex.AppIndexApi.end(client, action);
        }

        client.disconnect();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private Action createAction() {
        CustomSchemeGenerator.ViewInfo viewInfo = createViewInfo();
        if (viewInfo != null) {
            String title = (String) getSupportActionBar().getTitle();
            title = title != null ? title : getString(R.string.w_filter_title);
            return Action.newAction(
                    Action.TYPE_VIEW,
                    title,
                    CustomSchemeGenerator.createWebUri(viewInfo.category, viewInfo.viewType),
                    CustomSchemeGenerator.createAppUri(viewInfo.category, viewInfo.viewType));
        }
        return null;
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected CustomSchemeGenerator.ViewInfo createViewInfo() {
        return null;
    }
}
