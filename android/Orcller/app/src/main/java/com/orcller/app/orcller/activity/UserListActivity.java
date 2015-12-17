package com.orcller.app.orcller.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.ListEntity;
import com.orcller.app.orcller.widget.UserListView;

import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 12/12/15.
 */
abstract public class UserListActivity extends PSActionBarActivity implements UserListView.Delegate {
    protected static final String USER_ID_KEY = "userId";
    protected long userId;
    private UserListView userListView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_list);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        userId = getIntent().getLongExtra(USER_ID_KEY, 0);
        userListView = (UserListView) findViewById(R.id.userListView);

        userListView.setDelegate(this);
        userListView.setDataSource(createDataSource());
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * UserListView.Delgate
     */
    public void onLoad(UserListView listView) {
        if (listView.isFirstLoading())
            ProgressBarManager.show();
    }

    public void onLoadComplete(UserListView listView, ListEntity listEntity) {
        ProgressBarManager.hide();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    /**
     * @abstract
     */
    abstract protected UserListView.DataSource createDataSource();
}