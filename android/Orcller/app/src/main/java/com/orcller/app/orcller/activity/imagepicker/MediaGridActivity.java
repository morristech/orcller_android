package com.orcller.app.orcller.activity.imagepicker;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.MediaListActivity;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.ImagePickerMediaItemView;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.model.converter.MediaConverter;
import com.orcller.app.orcllermodules.caches.FBPhotoCaches;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Resources;
import pisces.psuikit.event.ImagePickerEvent;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 11/27/15.
 */
abstract public class MediaGridActivity extends PSActionBarActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String CHOICE_MODE_KEY = "choiceMode";
    protected ArrayList<Media> items = new ArrayList<>();
    private Error loadError;
    private RelativeLayout container;
    private SwipeRefreshLayout swipeRefreshLayout;
    private GridView gridView;
    private GridViewAdapter gridViewAdapter;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mediagrid);
        setToolbar((Toolbar) findViewById(R.id.toolbar));

        container = (RelativeLayout) findViewById(R.id.container);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        gridView = (GridView) findViewById(R.id.gridView);
        gridViewAdapter = new GridViewAdapter(this);

        exceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NoListData, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, container));
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_purple_accent);
        swipeRefreshLayout.setOnRefreshListener(this);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        gridView.setChoiceMode(getIntent().getIntExtra(CHOICE_MODE_KEY, AbsListView.CHOICE_MODE_MULTIPLE));
        loadContent();
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (gridView.getChoiceMode() > AbsListView.CHOICE_MODE_SINGLE) {
            Application.getTopActivity().getMenuInflater().inflate(pisces.android.R.menu.menu_image_picker, menu);

            MenuItem item = menu.findItem(pisces.android.R.id.select);
            item.setEnabled(gridView.getCheckedItemCount() > 0);
            int count = gridView.getCheckedItemCount();
            String prefix = getResources().getString(pisces.android.R.string.imagepicker_menu_select);
            String text = count > 0 ? prefix + " " + String.valueOf(count) : prefix;

            item.setTitle(text);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == pisces.android.R.id.select) {
            select();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        endDataLoading();
        gridView.setOnItemClickListener(null);
        gridView.setOnItemLongClickListener(null);
        FBSDKRequestQueue.currentQueue().clear();
        FBPhotoCaches.getDefault().clear();
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        swipeRefreshLayout.setRefreshing(false);
        exceptionViewManager.validate();
    }

    @Override
    public void onClick(ExceptionView view) {
        exceptionViewManager.clear();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        loadContent();
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NoListData.equals(view.getTag()))
            return loadError == null && items.size() < 1;

        if (ExceptionViewFactory.Type.NetworkError.equals(view.getTag())) {
            if (Application.isNetworkConnected())
                return false;
            if (items.size() > 0) {
                Toast.makeText(
                        Application.getTopActivity(),
                        Resources.getString(R.string.m_exception_title_error_network_long),
                        Toast.LENGTH_LONG)
                        .show();
                return false;
            }
            return true;
        }

        if (ExceptionViewFactory.Type.UnknownError.equals(view.getTag()))
            return loadError != null;

        return false;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (gridView.getChoiceMode() > AbsListView.CHOICE_MODE_SINGLE)
            invalidateOptionsMenu();
        else
            select();
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        MediaListActivity.show(items, position);
        return true;
    }

    public void onRefresh() {
        loadContent();
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

    protected boolean invalidDataLoading(String after) {
        boolean invalid = invalidDataLoading();

        if (!invalid) {
            loadError = null;

            if (isFirstLoading() && after == null) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
            }
        }

        return invalid;
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
            } else {
                endDataLoading();
            }
        } else {
            loadError = items.size() < 1 ? error : null;

            endDataLoading();
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    public void select() {
        final Object self = this;

        new AsyncTask<Void, Void, List<Media>>() {
            @Override
            protected List<Media> doInBackground(Void... params) {
                final List<Media> list = new ArrayList<>();

                SparseBooleanArray array = gridView.getCheckedItemPositions();
                for (int i=0; i<array.size(); i++) {
                    int key = array.keyAt(i);
                    if (array.get(key))
                        list.add(items.get(key));
                }

                return list;
            }

            @Override
            protected void onPostExecute(List<Media> result) {
                super.onPostExecute(result);

                EventBus.getDefault().post(
                        new ImagePickerEvent(
                                ImagePickerEvent.COMPLETE_SELECTION,
                                self,
                                result));
            }
        }.execute();

        Application.run(new Runnable() {
            @Override
            public void run() {
            }
        }, new Runnable() {
            @Override
            public void run() {
            }
        });
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
                itemView.setAllowsShowIndicator(gridView.getChoiceMode() > AbsListView.CHOICE_MODE_SINGLE);
                itemView.setLayoutParams(new AbsListView.LayoutParams(gridView.getColumnWidth(), gridView.getColumnWidth()));
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
