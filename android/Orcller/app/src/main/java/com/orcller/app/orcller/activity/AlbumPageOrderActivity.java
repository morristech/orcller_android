package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.event.PageListEvent;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Page;

import java.util.Collections;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
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

    @Override
    protected void done() {
        EventBus.getDefault().post(new PageListEvent(PageListEvent.PAGE_ORDER_CHANGE_COMPLETE, this, clonedModel.pages.data));
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
        Page p1 = getClonedModel().pages.getPageAtIndex(positions[0]);
        Page p2 = getClonedModel().pages.getPageAtIndex(positions[1]);
        int order1 = p1.order;
        int order2 = p2.order;
        p1.order = order2;
        p2.order = order1;

        Collections.swap(getClonedModel().pages.data, positions[0], positions[1]);
        gridViewAdapter.notifyDataSetChanged();
        gridView.clearChoices();
        setButtonEnabled();
    }

    private void setButtonEnabled() {
        getDoneItem().setEnabled(!getModel().pages.data.equals(getClonedModel().pages.data));
    }
}
