package com.orcller.app.orcller.activity.imagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AbsListView;

import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.facebook.FBAlbum;
import com.orcller.app.orcllermodules.model.facebook.FBMediaList;
import com.orcller.app.orcllermodules.model.facebook.FBPhotos;
import com.orcller.app.orcllermodules.model.facebook.FBVideoAlbum;
import com.orcller.app.orcllermodules.model.facebook.FBVideos;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import pisces.psfoundation.ext.Application;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/26/15.
 */
public class FBMediaGridActivity extends MediaGridActivity {
    public static final String ALBUM_KEY = "album";
    private FBAlbum album;
    private FBMediaList lastResult;

    // ================================================================================================
    //  Overridden: MediaGridActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        album = (FBAlbum) getIntent().getSerializableExtra(ALBUM_KEY);

        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(album.name);
    }

    @Override
    protected void loadContent() {
        loadMedia(null);
    }

    @Override
    protected void loadMore(int position) {
        if (lastResult != null && lastResult.paging.hasNext() && position >= items.size() - 9)
            loadMedia(lastResult.paging.cursors.after);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(FBAlbum album, int choiceMode) {
        Intent intent = new Intent(Application.applicationContext(), FBMediaGridActivity.class);
        intent.putExtra(ALBUM_KEY, album);
        intent.putExtra(CHOICE_MODE_KEY, choiceMode);
        Application.getTopActivity().startActivity(intent);
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

    private void loadMedia(final String after) {
        if (album == null || invalidDataLoading())
            return;

        if (album instanceof FBVideoAlbum) {
            load(after, FBVideos.class, new FBSDKRequest.CompleteHandler<FBVideos>() {
                @Override
                public void onComplete(FBVideos result, APIError error) {
                    if (error == null)
                        lastResult = result;

                    loadComplete(result.data, error, after == null);
                }
            });
        } else {
            load(after, FBPhotos.class, new FBSDKRequest.CompleteHandler<FBPhotos>() {
                @Override
                public void onComplete(FBPhotos result, APIError error) {
                    if (error == null)
                        lastResult = result;

                    loadComplete(result.data, error, after == null);
                }
            });
        }
    }
}
