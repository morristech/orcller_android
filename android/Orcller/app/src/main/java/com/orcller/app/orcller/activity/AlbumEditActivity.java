package com.orcller.app.orcller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.manager.MediaUploadUnit;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;

import pisces.psfoundation.ext.Application;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/3/15.
 */
public class AlbumEditActivity extends AlbumCreateActivity {
    private static final String ALBUM_ID_KEY = "albumId";

    // ================================================================================================
    //  Overridden: AlbumCreateActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.w_title_edit_album));
        loadAlbum();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        getPostItem().setTitle(R.string.w_save);
        return result;
    }

    @Override
    protected Album createModel() {
        return null;
    }

    @Override
    protected void doRequest() {
        MediaManager.getDefault().getUnit(clonedModel).setCompletionState(MediaUploadUnit.CompletionState.Modification);
        MediaManager.getDefault().startUploading(clonedModel);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(long albumId) {
        Intent intent = new Intent(Application.applicationContext(), AlbumEditActivity.class);
        intent.putExtra(ALBUM_ID_KEY, albumId);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void loadAlbum() {
        long albumId = getIntent().getLongExtra(ALBUM_ID_KEY, 0);

        if (albumId > 0) {
            final Activity activity = this;

            ProgressBarManager.show(this);
            AlbumDataProxy.getDefault().view(albumId, new Callback<ApiAlbum.AlbumRes>() {
                @Override
                public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                    ProgressBarManager.hide(activity);

                    if (response.isSuccess() && response.body().isSuccess()) {
                        setModel(response.body().entity);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    ProgressBarManager.hide(activity);
                }
            });
        }
    }
}
