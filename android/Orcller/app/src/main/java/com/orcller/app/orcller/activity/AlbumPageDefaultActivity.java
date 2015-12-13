package com.orcller.app.orcller.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.event.PageListEvent;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.widget.AlbumGridView;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;

/**
 * Created by pisces on 12/2/15.
 */
public class AlbumPageDefaultActivity extends PSActionBarActivity implements AlbumGridView.Delegate  {
    protected static final String ALBUM_KEY = "album";
    protected boolean allowsShowCloseAlert;
    private AlbumGridView gridView;
    private Album model;
    private Album clonedModel;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_pagedefault);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.w_title_album_page_default));

        allowsShowCloseAlert = true;
        gridView = (AlbumGridView) findViewById(R.id.gridView);

        gridView.setDelegate(this);
        setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        gridView = null;
        model = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (allowsShowCloseAlert && getDoneItem().isEnabled()) {
                    showCloseAlert();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Application.getTopActivity().getMenuInflater().inflate(R.menu.menu_album_info_edit, menu);
        getDoneItem().setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (allowsShowCloseAlert && getDoneItem().isEnabled()) {
                    showCloseAlert();
                    return true;
                }

                onBackPressed();
                break;

            case R.id.done:
                EventBus.getDefault().post(new PageListEvent(PageListEvent.PAGE_DEFAULT_CHANGE_COMPLETE, this, clonedModel));
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album album) {
        Intent intent = new Intent(Application.applicationContext(), AlbumPageDefaultActivity.class);
        intent.putExtra(ALBUM_KEY, album);
        Application.startActivity(intent, R.animator.fadein, R.animator.fadeout);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * AlbumGridView delegate
     */
    public void onSelect(int position) {
        clonedModel.default_page_index = SharedObject.convertPositionToPageIndex(position);
        gridView.reload();
        getDoneItem().setEnabled(model.default_page_index != clonedModel.default_page_index);
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected Album getClonedModel() {
        return clonedModel;
    }

    protected Album getModel() {
        return model;
    }

    protected void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    protected void modelChanged() {
        try {
            clonedModel = (Album) model.clone();
            gridView.setModel(clonedModel);
            gridView.setSelectedIndex(SharedObject.convertPageIndexToPosition(clonedModel.default_page_index));
        } catch (CloneNotSupportedException e) {
            if (BuildConfig.DEBUG)
                Log.d(e.getMessage());
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private MenuItem getDoneItem() {
        return getToolbar().getMenu().findItem(R.id.done);
    }

    private void showCloseAlert() {
        AlertDialogUtils.show(getString(R.string.m_activity_close_message),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == AlertDialog.BUTTON_POSITIVE) {
                            onBackPressed();
                        }
                    }
                },
                getString(R.string.w_no),
                getString(R.string.w_yes)
        );
    }
}
