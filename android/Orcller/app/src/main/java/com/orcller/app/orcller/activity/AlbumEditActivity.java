package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.manager.MediaUploadUnit;
import com.orcller.app.orcller.model.album.Album;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 12/3/15.
 */
public class AlbumEditActivity extends AlbumCreateActivity {

    // ================================================================================================
    //  Overridden: AlbumCreateActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.w_title_edit_album));
    }

    @Override
    protected void doRequest() {
        MediaManager.getDefault().getUnit(clonedModel).setCompletionState(MediaUploadUnit.CompletionState.Creation);
        MediaManager.getDefault().startUploading(clonedModel);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album album) {
        Intent intent = new Intent(Application.applicationContext(), AlbumEditActivity.class);
        intent.putExtra(ALBUM_KEY, album);
        Application.getTopActivity().startActivity(intent);
    }
}
