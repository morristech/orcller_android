package com.orcller.app.orcller.activity.imagepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.FBImagePickerItemView;
import com.orcller.app.orcllermodules.caches.FBPhotoCaches;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.facebook.FBAlbum;
import com.orcller.app.orcllermodules.model.facebook.FBAlbums;
import com.orcller.app.orcllermodules.model.facebook.FBVideoAlbum;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import pisces.psuikit.utils.AlertDialogUtils;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/26/15.
 */
public class FBImagePickerActivity extends PSActionBarActivity
        implements AdapterView.OnItemClickListener {
    private int choiceMode;
    private List<FBAlbum> items = new ArrayList<>();
    private ListView listView;
    private ListAdapter listAdapter;
    private FBAlbums lastResult;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_facebook_imagepicker);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.w_facebook_photos));

        choiceMode = getIntent().getIntExtra(MediaGridActivity.CHOICE_MODE_KEY, AbsListView.CHOICE_MODE_MULTIPLE);
        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new ListAdapter(this);

        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
        loadAlbums();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ProgressBarManager.hide(this);
        listView.setOnItemClickListener(null);
        FBSDKRequestQueue.currentQueue().clear();
        FBPhotoCaches.getDefault().clear();
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(int choiceMode) {
        Intent intent = new Intent(Application.applicationContext(), FBImagePickerActivity.class);
        intent.putExtra(MediaGridActivity.CHOICE_MODE_KEY, choiceMode);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FBMediaGridActivity.show(items.get(position), choiceMode);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void initItems() {
        FBVideoAlbum album = new FBVideoAlbum();
        album.name = getString(R.string.w_video);
        items.clear();
        items.add(album);
    }

    private void load(final String after) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "count,cover_photo,created_time,id,name,updated_time");

        if (after != null) {
            parameters.putString("after", after);
        } else {
            ProgressBarManager.show(this);
        }

        FBSDKRequestQueue.currentQueue().request(
                "me/albums",
                parameters,
                FBAlbums.class,
                new FBSDKRequest.CompleteHandler<FBAlbums>() {
                    @Override
                    public void onComplete(final FBAlbums result, APIError error) {
                        if (error == null) {
                            lastResult = result;

                            Application.run(new Runnable() {
                                @Override
                                public void run() {
                                    if (after == null)
                                        initItems();

                                    for (FBAlbum album : lastResult.data) {
                                        if (album.count > 0)
                                            items.add(album);
                                    }
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    endDataLoading();
                                    listAdapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            endDataLoading();

                            String message = error.getMessage();
                            if (message != null)
                                AlertDialogUtils.show(message, getResources().getString(R.string.w_dismiss));
                        }
                    }
                }
        );
    }

    private void loadAlbums() {
        if (FBSDKRequestQueue.currentQueue().isValidAccessToken()) {
            load(null);
        } else {
            AuthenticationCenter.getDefault().syncWithFacebook(this, new Api.CompleteHandler() {
                @Override
                public void onComplete(Object result, APIError error) {
                    if (error == null) {
                        load(null);
                    } else {
                        String message = error.getMessage();
                        if (message != null) {
                            AlertDialogUtils.show(message,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == AlertDialog.BUTTON_POSITIVE)
                                                loadAlbums();
                                        }
                                    },
                                    getResources().getString(R.string.w_dismiss),
                                    getResources().getString(R.string.w_retry)
                            );
                        }
                    }
                }
            });
        }
    }

    // ================================================================================================
    //  Class: ListAdapter
    // ================================================================================================

    private class ListAdapter extends BaseAdapter {
        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public FBAlbum getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FBImagePickerItemView itemView;

            if (convertView == null) {
                itemView = new FBImagePickerItemView(context);
                convertView = itemView;
            } else {
                itemView = (FBImagePickerItemView) convertView;
            }

            itemView.setModel(getItem(position));

            if (lastResult != null && lastResult.paging.hasNext() && position >= items.size() - 3) {
                load(lastResult.paging.cursors.after);
            }

            return convertView;
        }
    }
}
