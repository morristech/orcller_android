package com.orcller.app.orcller.activity;

import android.content.Intent;

import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.UserListView;

import pisces.psfoundation.ext.Application;
import retrofit.Call;

/**
 * Created by pisces on 12/10/15.
 */
public class AlbumHeartListActivity extends AlbumAdditionalInfoListActivity {

    // ================================================================================================
    //  Overridden: AlbumAdditionalInfoListActivity
    // ================================================================================================

    @Override
    protected UserListView.DataSource createDataSource() {
        return new UserListView.DataSource<ApiAlbum.LikesRes>() {
            @Override
            public boolean followButtonHidden() {
                return true;
            }

            @Override
            public Call<ApiAlbum.LikesRes> createDataLoadCall(int limit, String after) {
                return AlbumDataProxy.getDefault().service().likes(albumId, limit, after);
            }
        };
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(long albumId) {
        Intent intent = new Intent(Application.applicationContext(), AlbumHeartListActivity.class);
        intent.putExtra(ALBUM_ID_KEY, albumId);
        Application.getTopActivity().startActivity(intent);
    }
}