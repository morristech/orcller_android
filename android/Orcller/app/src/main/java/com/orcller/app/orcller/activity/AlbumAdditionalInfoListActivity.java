package com.orcller.app.orcller.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.album.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.album.ListEntity;
import com.orcller.app.orcller.widget.UserListView;

import pisces.psuikit.ext.PSActionBarActivity;

/**
 * Created by pisces on 12/10/15.
 */
abstract public class AlbumAdditionalInfoListActivity extends PSActionBarActivity implements UserListView.OnLoadListener {
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

        albumId = getIntent().getLongExtra(ALBUM_ID_KEY, 0);
        userListView = (UserListView) findViewById(R.id.userListView);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);
        userListView.setDataSource(createDataSource());
        userListView.setOnLoadListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        userListView = null;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onLoad(ListEntity listEntity) {
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
