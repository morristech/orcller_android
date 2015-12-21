package com.orcller.app.orcller.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.UserListView;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 12/10/15.
 */
abstract public class AlbumAdditionalInfoListActivity extends PSActionBarActivity implements UserListView.Delegate {
    protected static final String ALBUM_ID_KEY = "albumId";
    protected long albumId;
    private UserListView userListView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_additionalinfo_list);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        albumId = getIntent().getLongExtra(ALBUM_ID_KEY, 0);
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
    public void onFailure(UserListView listView, Error error) {
        ProgressBarManager.hide();
    }

    public void onLoad(UserListView listView) {
        if (listView.isFirstLoading())
            ProgressBarManager.show();
    }

    public void onLoadComplete(UserListView listView, ListEntity listEntity) {
        ProgressBarManager.hide();
        getSupportActionBar().setTitle(
                SharedObject.getAlbumInfoText((AlbumAdditionalListEntity) listEntity));
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    /**
     * @abstract
     */
    abstract protected UserListView.DataSource createDataSource();
}