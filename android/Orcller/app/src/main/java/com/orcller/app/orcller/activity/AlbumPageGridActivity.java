package com.orcller.app.orcller.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.AlbumPageGridItemView;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Page;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.event.Event;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSGridView;

/**
 * Created by pisces on 12/1/15.
 */
public class AlbumPageGridActivity extends PSActionBarActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener {
    protected static final String ALBUM_KEY = "album";
    protected boolean allowsShowCloseAlert;
    protected Button doneButton;
    protected GridViewAdapter gridViewAdapter;
    private PSGridView gridView;
    private Album model;
    private Album clonedModel;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_pagegrid);
        setToolbar((Toolbar) findViewById(R.id.toolbar));

        allowsShowCloseAlert = true;
        doneButton = (Button) findViewById(R.id.doneButton);
        gridView = (PSGridView) findViewById(R.id.gridView);
        gridViewAdapter = new GridViewAdapter(this);

        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(this);
        doneButton.setOnClickListener(this);
        setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        gridView.setOnItemClickListener(null);
        gridView.setOnItemLongClickListener(null);
        doneButton.setOnClickListener(null);

        gridView = null;
        gridViewAdapter = null;
        doneButton = null;
        model = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (allowsShowCloseAlert && doneButton.isEnabled()) {
                    showCloseAlert();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (allowsShowCloseAlert && doneButton.isEnabled()) {
                    showCloseAlert();
                    return true;
                }

                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    @Override
    public void onClick(View v) {
        EventBus.getDefault().post(new Event(Event.EDIT_COMPLETE, this, clonedModel));
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            gridViewAdapter.setModel(clonedModel);
        } catch (CloneNotSupportedException e) {
            if (BuildConfig.DEBUG)
                Log.d(e.getMessage());
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

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

    // ================================================================================================
    //  Class: GridViewAdapter
    // ================================================================================================

    protected class GridViewAdapter extends BaseAdapter {
        private Album model;
        private Context context;

        public GridViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return model != null ? model.pages.data.size() : 0;
        }

        @Override
        public Page getItem(int position) {
            return model.pages.getPageAtIndex(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AlbumPageGridItemView itemView;

            if (convertView == null) {
                itemView = new AlbumPageGridItemView(context);
                itemView.setLayoutParams(new ViewGroup.LayoutParams(gridView.getColumnWidth(), gridView.getColumnWidth()));
                convertView = itemView;
            } else {
                itemView = (AlbumPageGridItemView) convertView;
            }

            Page page = getItem(position);
            itemView.setEnabled(model.isMine() || page.isMine());
            itemView.setPage(page);
            itemView.setText(String.valueOf(position + 1));

            return convertView;
        }

        public void setModel(Album model) {
            this.model = model;

            notifyDataSetChanged();
        }
    }

    // ================================================================================================
    //  Class: GridViewAdapter
    // ================================================================================================

    public static class Event extends pisces.psfoundation.event.Event {
        public static final String EDIT_COMPLETE = "editComplete";

        public Event(String type, Object target, Object object) {
            super(type, target, object);
        }
    }
}
