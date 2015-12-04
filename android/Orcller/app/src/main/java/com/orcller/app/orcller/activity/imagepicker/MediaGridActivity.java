package com.orcller.app.orcller.activity.imagepicker;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.MediaListActivity;
import com.orcller.app.orcller.itemview.ImagePickerMediaItemView;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.converter.MediaConverter;
import com.orcller.app.orcllermodules.caches.FBPhotoCaches;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psuikit.event.ImagePickerEvent;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/27/15.
 */
abstract public class MediaGridActivity extends PSActionBarActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {
    protected ArrayList<Media> items = new ArrayList<>();
    private Button selectButton;
    private GridView gridView;
    private GridViewAdapter gridViewAdapter;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutRes());
        setToolbar((Toolbar) findViewById(R.id.toolbar));

        selectButton = (Button) findViewById(R.id.selectButton);
        gridView = (GridView) findViewById(R.id.gridView);
        gridViewAdapter = new GridViewAdapter(this);

        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        selectButton.setOnClickListener(this);
        loadContent();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        ProgressBarManager.hide(this);
        gridView.setOnItemClickListener(null);
        gridView.setOnItemLongClickListener(null);
        selectButton.setOnClickListener(null);
        FBSDKRequestQueue.currentQueue().clear();
        FBPhotoCaches.getDefault().clear();

        gridView = null;
        gridViewAdapter = null;
        items = null;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide(this);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    @Override
    public void onClick(View v) {
        final List<Media> list = new ArrayList<>();
        final Object self = this;

        Application.run(new Runnable() {
            @Override
            public void run() {
                SparseBooleanArray array = gridView.getCheckedItemPositions();
                for (int i=0; i<array.size(); i++) {
                    int key = array.keyAt(i);
                    if (array.get(key))
                        list.add(items.get(key));
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(
                        new ImagePickerEvent(
                                ImagePickerEvent.COMPLETE_SELECTION,
                                self,
                                list));
                finish();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int count = gridView.getCheckedItemCount();
        String prefix = getResources().getString(pisces.android.R.string.imagepicker_menu_select);
        String text = count > 0 ? prefix + " " + String.valueOf(count) : prefix;

        selectButton.setEnabled(gridView.getCheckedItemCount() > 0);
        selectButton.setText(text);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        MediaListActivity.show(items, position);
        return true;
    }

    public void onEventMainThread(Object event) {
        if (event instanceof IndexChangeEvent) {
            IndexChangeEvent casted = (IndexChangeEvent) event;

            if (casted.getTarget() instanceof MediaListActivity)
                gridView.setSelection(casted.getSelectedIndex());
        }
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    abstract protected void loadContent();

    abstract protected void loadMore(int loadMore);

    protected @LayoutRes int getLayoutRes() {
        return R.layout.activity_mediagrid;
    }

    protected void loadComplete(final List<?> data, Error error, final boolean refresh) {
        if (error == null) {
            if (data.size() > 0) {
                Application.run(new Runnable() {
                    @Override
                    public void run() {
                        if (refresh)
                            items.clear();

                        for (Object object : data) {
                            items.add(MediaConverter.convert(object));
                        }
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        endDataLoading();
                        gridViewAdapter.notifyDataSetChanged();

                        if (refresh) {
                            gridView.clearChoices();
                            onItemClick(gridView, null, 0, 0);
                        }
                    }
                });
            }
        } else {
            endDataLoading();
        }
    }

    // ================================================================================================
    //  Class: GridViewAdapter
    // ================================================================================================

    private class GridViewAdapter extends BaseAdapter {
        private Context context;

        public GridViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Media getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImagePickerMediaItemView itemView;

            if (convertView == null) {
                itemView = new ImagePickerMediaItemView(context);
                itemView.setLayoutParams(new ViewGroup.LayoutParams(gridView.getColumnWidth(), gridView.getColumnWidth()));
                convertView = itemView;
            } else {
                itemView = (ImagePickerMediaItemView) convertView;
            }

            itemView.setModel(getItem(position));
            loadMore(position);

            return convertView;
        }
    }
}
