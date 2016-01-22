package com.orcller.app.orcller.manager;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.event.AlbumEvent;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Image;
import com.orcller.app.orcller.model.Page;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.model.api.ApiMedia;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.MediaDataProxy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.URLUtils;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/3/15.
 */
public class MediaUploadUnit implements Serializable {
    public enum CompletionState {
        None,
        Creation,
        Modification
    }

    private boolean cancelled;
    private boolean processing;
    private float progressFloat;
    private int progress;
    private int current;
    private int total;
    private CompletionState completionState = CompletionState.None;
    private ConcurrentLinkedQueue<Image> queue = new ConcurrentLinkedQueue<>();
    private HashMap<String, Integer> map = new HashMap<>();
    private transient Delegate delegate;
    private Album model;

    public MediaUploadUnit(Album model) {
        this.model = model;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isProcessing() {
        return processing;
    }

    public CompletionState getCompletionState() {
        return completionState;
    }

    public void setCompletionState(CompletionState completionState) {
        this.completionState = completionState;
    }
    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public Album getModel() {
        return model;
    }

    public void cancelAll() {
        cancelled = true;
        AWSManager.getTransferUtility().cancelAllWithType(TransferType.UPLOAD);
        map.clear();
        queue.clear();
    }

    public float getProgress() {
        return progressFloat;
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

    public void pauseAll() {
        AWSManager.getTransferUtility().cancelAllWithType(TransferType.UPLOAD);
        map.clear();
        processing = false;
    }

    public void upload() {
        if (isProcessing())
            return;

        if (delegate != null)
            delegate.onStartUploading(this);

        EventBus.getDefault().post(new Event(Event.START_UPLOADING, this));

        if (queue.size() > 0) {
            startUploading();
        } else {
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
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private boolean checkCompletion() {
        if (delegate != null)
            delegate.onProcessUploading(this);

        EventBus.getDefault().post(new Event(Event.PROCESS_UPLOADING, this));

        if (progress < total)
            return false;

        if (current >= total) {
            executeCompletionState();
            processing = false;
            return true;
        }

        startUploading();

        return false;
    }

    private void enqueue(Image image) {
        if (URLUtils.isLocal(image.url) && !queue.contains(image))
            queue.offer(image);
    }

    private void errorState() {
        AWSManager.getTransferUtility().cancelAllWithType(TransferType.UPLOAD);

        if (delegate != null)
            delegate.onFailUploading(this);

        EventBus.getDefault().post(new Event(Event.FAILED_UPLOADING, this));
        processing = false;
    }

    private void executeCompletionState() {
        if (delegate != null)
            delegate.onCompleteUploading(this);

        EventBus.getDefault().post(new Event(Event.COMPLETE_UPLOADING, this));

        if (completionState.equals(CompletionState.Creation)) {
            requestAlbumCreation();
        } else if (completionState.equals(CompletionState.Modification)) {
            requestAlbumModification();
        }
    }

    private void executeUploading() {
        ConcurrentLinkedQueue<Image> copiedQueue = new ConcurrentLinkedQueue<>(queue);
        int count = copiedQueue.size();

        for (int i=0; i<count; i++) {
            final Image image = copiedQueue.poll();
            final String key = String.valueOf(image.hashCode());
            int retryCount = map.containsKey(key) ? map.get(key) : 0;

            if (retryCount < 3) {
                map.put(key, retryCount + 1);

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
                                            queue.remove(image);

                                            if (error != null)
                                                queue.offer(image);

                                            processUploading(error == null);
                                        }
                                    });
                        } else {
                            queue.remove(image);
                            queue.offer(image);
                            processUploading(false);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        queue.remove(image);
                        queue.offer(image);
                        processUploading(false);
                    }
                });
            } else {
                map.clear();
                errorState();
                break;
            }
        }
    }

    private void processUploading(Boolean isSuccess) {
        if (isSuccess)
            current++;

        progress++;
        progressFloat = (float) current / total;

        MediaManager.getDefault().saveCacheFile();
        checkCompletion();
    }

    private void requestAlbumCreation() {
        final Object target = this;

        AlbumDataProxy.getDefault().create(model, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    EventBus.getDefault().post(new AlbumEvent(AlbumEvent.CREATE, target, response.body().entity));
                    MediaManager.getDefault().completeUploading(model);
                } else {
                    if (DEBUG)
                        e("Api Error", response.body());

                    errorState();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                errorState();
            }
        });
    }

    private void requestAlbumModification() {
        final Object target = this;
        AlbumDataProxy.getDefault().update(model, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    MediaManager.getDefault().completeUploading(model);
                    EventBus.getDefault().post(new AlbumEvent(AlbumEvent.MODIFY, target, response.body().entity));
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
        current = 0;
        progress = 0;
        total = queue.size();

        if (!checkCompletion()) {
            executeUploading();
        }
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public interface Delegate {
        void onCompleteUploading(MediaUploadUnit unit);
        void onFailUploading(MediaUploadUnit unit);
        void onProcessUploading(MediaUploadUnit unit);
        void onStartUploading(MediaUploadUnit unit);
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
