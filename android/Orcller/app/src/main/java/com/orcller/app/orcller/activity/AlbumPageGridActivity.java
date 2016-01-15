package com.orcller.app.orcller.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.event.PageListEvent;
import com.orcller.app.orcller.itemview.AlbumPageGridItemView;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Page;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSGridView;

/**
 * Created by pisces on 12/1/15.
 */
public class AlbumPageGridActivity extends PSActionBarActivity implements AdapterView.OnItemClickListener {
    protected static final String ALBUM_KEY = "album";
    protected GridViewAdapter gridViewAdapter;
    protected PSGridView gridView;
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

        gridView = (PSGridView) findViewById(R.id.gridView);
        gridViewAdapter = new GridViewAdapter(this);

        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(this);
        setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
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
            case R.id.done:
                EventBus.getDefault().post(new PageListEvent(PageListEvent.PAGE_EDIT_COMPLETE, this, clonedModel));
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        gridView.setOnItemClickListener(null);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected Album getClonedModel() {
        return clonedModel;
    }

    protected MenuItem getDoneItem() {
        return getToolbar().getMenu().findItem(R.id.done);
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
                itemView.setLayoutParams(new AbsListView.LayoutParams(gridView.getColumnWidth(), gridView.getColumnWidth()));
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
}
