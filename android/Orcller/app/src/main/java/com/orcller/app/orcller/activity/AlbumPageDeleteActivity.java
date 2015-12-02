package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Album;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 12/2/15.
 */
public class AlbumPageDeleteActivity extends AlbumPageGridActivity {

    // ================================================================================================
    //  Overridden: AlbumPageDeleteActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.w_title_album_page_delete));
    }

    @Override
    public void onClick(View v) {
        for (int postion : gridView.getCheckedPositions()) {
            getClonedModel().removePage(postion);
        }

        super.onClick(v);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        doneButton.setEnabled(gridView.getCheckedItemCount() > 0);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album album) {
        Intent intent = new Intent(Application.applicationContext(), AlbumPageDeleteActivity.class);
        intent.putExtra(ALBUM_KEY, album);
        Application.startActivity(intent, R.animator.fadein, R.animator.fadeout);
    }
}
