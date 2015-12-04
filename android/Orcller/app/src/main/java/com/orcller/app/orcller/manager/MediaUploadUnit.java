package com.orcller.app.orcller.manager;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.orcller.app.orcller.event.AlbumEvent;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Image;
import com.orcller.app.orcller.model.album.Page;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.model.api.ApiMedia;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.MediaDataProxy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.URLUtils;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/3/15.
 */
public class MediaUploadUnit implements Serializable {
    public enum CompletionState {
        None,
        Creation,
        Modification
    }

    private boolean processing;
    private float process;
    private int total;
    private int current;
    private CompletionState completionState = CompletionState.None;
    private ConcurrentLinkedQueue<Image> queue = new ConcurrentLinkedQueue<>();
    private HashMap<String, Image> map = new HashMap<>();
    private Album model;

    public MediaUploadUnit(Album model) {
        this.model = model;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isProcessing() {
        return processing;
    }

    public float getProcess() {
        return process;
    }

    public CompletionState getCompletionState() {
        return completionState;
    }

    public void setCompletionState(CompletionState completionState) {
        this.completionState = completionState;
    }

    public Album getModel() {
        return model;
    }

    public void cancelAll() {
        AWSManager.getTransferUtility().cancelAllWithType(TransferType.UPLOAD);
        map.clear();
        queue.clear();
    }

    public void clearAll() {
        cancelAll();

        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                for (Page page : model.pages.data) {
                    MediaManager.getDefault().deleteFiles(page.media.images);
                }
            }
        });
    }

    public void upload() {
        if (!isProcessing())
            EventBus.getDefault().post(new Event(Event.START_UPLOADING, this));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (Page page : model.pages.data) {
                    enqueue(page.media.images.standard_resolution);
                    enqueue(page.media.images.low_resolution);
                    enqueue(page.media.images.thumbnail);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                startUploading();
            }
        }.execute();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void checkCompletion() {
        EventBus.getDefault().post(new Event(Event.PROCESS_UPLOADING, this));

        if (current >= total) {
            executeCompletionState();
            processing = false;
        }
    }

    private void enqueue(Image image) {
        if (URLUtils.isLocal(image.url) && !queue.contains(image))
            queue.offer(image);
    }

    private void errorState() {
        cancelAll();
        queue.clear();
        EventBus.getDefault().post(new Event(Event.FAILED_UPLOADING, this));
        processing = false;
    }

    private void executeCompletionState() {
        EventBus.getDefault().post(new Event(Event.COMPLETE_UPLOADING, this));

        if (completionState.equals(CompletionState.Creation)) {
            requestAlbumCreation();
        } else if (completionState.equals(CompletionState.Modification)) {
            requestAlbumModification();
        }
    }

    private void executeUploading() {
        while (!queue.isEmpty()) {
            final Image image = queue.poll();
            final String key = String.valueOf(image.hashCode());

            if (!map.containsKey(key) && image != null) {
                map.put(key, image);

                MediaDataProxy.getDefault().getUploadInfo(new Callback<ApiMedia.UploadInfoRes>() {
                    @Override
                    public void onResponse(Response<ApiMedia.UploadInfoRes> response, Retrofit retrofit) {
                        if (response.isSuccess() && response.body().isSuccess()) {
                            ApiMedia.UploadInfoEntity entity = response.body().entity;

                            MediaManager.getDefault().uploadImage(
                                    image,
                                    entity.filename,
                                    new MediaManager.CompleteHandler() {
                                        @Override
                                        public void onComplete(Error error) {
                                            if (error == null) {
                                                processUploading();
                                            } else {
                                                errorState();
                                            }

                                            map.remove(key);
                                        }
                                    });
                        } else {
                            errorState();
                            map.remove(key);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        errorState();
                        map.remove(key);
                    }
                });
            }
        }
    }

    private void processUploading() {
        MediaManager.getDefault().saveCacheFile();

        current++;
        process = (float) current / total;

        checkCompletion();
    }

    private void requestAlbumCreation() {
        AlbumDataProxy.getDefault().create(model, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    MediaManager.getDefault().clearUploading(model);
                    EventBus.getDefault().post(new AlbumEvent(AlbumEvent.COMPLETE_CREATION, this, response.body().entity));
                } else {
                    errorState();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                errorState();
            }
        });
    }

    private void requestAlbumModification() {
        AlbumDataProxy.getDefault().update(model, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    MediaManager.getDefault().clearUploading(model);
                    EventBus.getDefault().post(new AlbumEvent(AlbumEvent.COMPLETE_MODIFICATION, this, response.body().entity));
                } else {
                    errorState();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                errorState();
            }
        });
    }

    private void startUploading() {
        processing = true;
        total = queue.size();
        current = 0;

        if (current >= total) {
            process = 1.0f;
            checkCompletion();
        } else {
            executeUploading();
        }
    }

    // ================================================================================================
    //  Class: Event
    // ================================================================================================

    public static class Event extends pisces.psfoundation.event.Event {
        public static final String COMPLETE_UPLOADING = "processUploading";
        public static final String FAILED_UPLOADING = "failedUploading";
        public static final String PROCESS_UPLOADING = "processUploading";
        public static final String START_UPLOADING = "startUploading";

        public Event(String type, Object target) {
            super(type, target);
        }

        public Event(String type, Object target, Object object) {
            super(type, target, object);
        }
    }
}