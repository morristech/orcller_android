package com.orcller.app.orcller.activity;

import android.content.Intent;

import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.UserListView;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import pisces.psfoundation.ext.Application;
import retrofit.Call;

/**
 * Created by pisces on 12/10/15.
 */
public class AlbumStarListActivity extends AlbumAdditionalInfoListActivity {

    // ================================================================================================
    //  Overridden: AlbumAdditionalInfoListActivity
    // ================================================================================================

    @Override
    protected UserListView.DataSource createDataSource() {
        return new UserListView.DataSource<ApiAlbum.FavoritesRes>() {
            @Override
            public boolean followButtonHidden() {
                return true;
            }

            @Override
            public Call<ApiAlbum.FavoritesRes> createDataLoadCall(int limit, String after) {
                return AlbumDataProxy.getDefault().service().favorites(albumId, limit, after);
            }

            @Override
            public AbstractDataProxy createDataProxy() {
                return AlbumDataProxy.getDefault();
            }
        };
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(long albumId) {
        Intent intent = new Intent(Application.applicationContext(), AlbumStarListActivity.class);
        intent.putExtra(ALBUM_ID_KEY, albumId);
        Application.getTopActivity().startActivity(intent);
    }
}
