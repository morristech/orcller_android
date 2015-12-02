package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Album;

import java.util.Collections;

import pisces.psfoundation.ext.Application;
import pisces.psuikit.ext.PSGridView;

/**
 * Created by pisces on 12/1/15.
 */
public class AlbumPageOrderActivity extends AlbumPageGridActivity {

    // ================================================================================================
    //  Overridden: AlbumPageGridActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.w_title_album_page_order));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!view.isEnabled())
            gridView.setItemChecked(position, false);

        if (gridView.getCheckedItemCount() == 2)
            changeOrder(gridView);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album album) {
        Intent intent = new Intent(Application.applicationContext(), AlbumPageOrderActivity.class);
        intent.putExtra(ALBUM_KEY, album);
        Application.startActivity(intent, R.animator.fadein, R.animator.fadeout);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void changeOrder(PSGridView gridView) {
        int[] positions = gridView.getCheckedPositions();
        Collections.swap(getClonedModel().pages.data, positions[0], positions[1]);
        gridViewAdapter.notifyDataSetChanged();
        gridView.clearChoices();
        setButtonEnabled();
    }

    private void setButtonEnabled() {
        doneButton.setEnabled(!getModel().pages.data.equals(getClonedModel().pages.data));
    }
}
