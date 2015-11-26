package com.orcller.app.orcller.activity.imagepicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.facebook.FBAlbum;
import com.orcller.app.orcllermodules.model.facebook.FBMediaList;
import com.orcller.app.orcllermodules.model.facebook.FBPhoto;
import com.orcller.app.orcllermodules.model.facebook.FBPhotos;
import com.orcller.app.orcllermodules.model.facebook.FBVideo;
import com.orcller.app.orcllermodules.model.facebook.FBVideoAlbum;
import com.orcller.app.orcllermodules.model.facebook.FBVideos;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/26/15.
 */
public class FBMediaGridActivity extends PSActionBarActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    public static final String ALBUM_KEY = "album";
    private ArrayList<Media> items = new ArrayList<>();
    private Button selectButton;
    private GridView gridView;
    private GridViewAdapter gridViewAdapter;
    private FBAlbum album;
    private FBMediaList lastResult;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fb_mediagrid);

        setToolbar((Toolbar) findViewById(R.id.toolbar));

        album = (FBAlbum) getIntent().getSerializableExtra(ALBUM_KEY);
        selectButton = (Button) findViewById(R.id.selectButton);
        gridView = (GridView) findViewById(R.id.gridView);
        gridViewAdapter = new GridViewAdapter(this);

        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        getSupportActionBar().setTitle(album.name);
        loadMedia(null);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        ProgressBarManager.hide(this);
        gridView.setOnItemClickListener(null);
        gridView.setOnItemLongClickListener(null);
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
    //  Public
    // ================================================================================================

    public static void startActivity(FBAlbum album) {
        Intent intent = new Intent(Application.applicationContext(), FBMediaGridActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ALBUM_KEY, album);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

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
        MediaListActivity.startActivity(items, position);
        return true;
    }

    public void onEventMainThread(Object event) {
        if (event instanceof MediaListActivity.OnChangeSelectedIndex) {
            MediaListActivity.OnChangeSelectedIndex casted = (MediaListActivity.OnChangeSelectedIndex) event;
            gridView.setSelection(casted.getSelectedIndex());
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(String after, Class clazz, FBSDKRequest.CompleteHandler completeHandler) {
        Bundle parameters = album.getParameters();

        if (after != null) {
            parameters.putString("after", after);
        } else {
            ProgressBarManager.show(this);
        }

        FBSDKRequestQueue.currentQueue().request(
                album.getGraphPath(),
                parameters,
                clazz,
                completeHandler
        );
    }

    private void loadComplete(FBMediaList result, APIError error, final boolean refresh) {
        if (error == null) {
            lastResult = result;

            if (lastResult.getData().size() > 0) {
                Application.run(new Runnable() {
                    @Override
                    public void run() {
                        if (refresh)
                            items.clear();

                        for (Object model : lastResult.getData()) {
                            if (model instanceof FBPhoto)
                                items.add(MediaConverter.convert((FBPhoto) model));
                            else if (model instanceof FBVideo)
                                items.add(MediaConverter.convert((FBVideo) model));
                        }
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        endDataLoading();
                        gridViewAdapter.notifyDataSetChanged();
                    }
                });
            }
        } else {
            endDataLoading();
        }
    }

    private void loadMedia(final String after) {
        if (album == null || invalidDataLoading())
            return;

        if (album instanceof FBVideoAlbum) {
            load(after, FBVideos.class, new FBSDKRequest.CompleteHandler<FBVideos>() {
                @Override
                public void onComplete(FBVideos result, APIError error) {
                    loadComplete(result, error, after == null);
                }
            });
        } else {
            load(after, FBPhotos.class, new FBSDKRequest.CompleteHandler<FBPhotos>() {
                @Override
                public void onComplete(FBPhotos result, APIError error) {
                    loadComplete(result, error, after == null);
                }
            });
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

            if (lastResult != null && lastResult.paging.hasNext() && position >= items.size() - 9) {
                loadMedia(lastResult.paging.cursors.after);
            }

            return convertView;
        }
    }
}
