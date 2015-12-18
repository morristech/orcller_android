package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Page;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                for (Object page : gridView.getCheckedItems()) {
                    getClonedModel().removePage((Page) page);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getDoneItem().setEnabled(gridView.getCheckedItemCount() > 0);
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
