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

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
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

    public enum UploadState {
        None,
        Cancelled,
        Failed,
        Processing,
        Complete
    }

    private float progressFloat;
    private int progress;
    private int current;
    private int total;
    private CompletionState completionState = CompletionState.None;
    private UploadState uploadState = UploadState.None;
    private ConcurrentLinkedQueue<Image> cachedQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Image> deleteQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Image> queue = new ConcurrentLinkedQueue<>();
    private HashMap<Integer, Integer> requestMap = new HashMap<>();
    private HashMap<Integer, Integer> uploadMap = new HashMap<>();
    private transient Delegate delegate;
    private Album model;

    public MediaUploadUnit(Album model) {
        this.model = model;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

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

    public float getProgress() {
        return progressFloat;
    }

    public UploadState getUploadState() {
        return uploadState;
    }

    public void setUploadMap(UploadState uploadState) {
        this.uploadState = uploadState;
    }

    public void cancelAll() {
        AWSManager.getTransferUtility().cancelAllWithType(TransferType.UPLOAD);
        requestMap.clear();
        uploadMap.clear();
        queue.clear();
        cachedQueue.clear();
        deleteQueue.clear();

        uploadState = UploadState.Cancelled;
        current = 0;
        progress = 0;
        total = 0;
    }

    public void clear(Page page) {
        if (page != null) {
            clear(page.media.images.thumbnail);
            clear(page.media.images.low_resolution);
            clear(page.media.images.standard_resolution);
        }
    }

    public void clear(Image image) {
        if (image == null || deleteQueue.contains(image))
            return;

        int key = image.hashCode();
        int uploadId = uploadMap.containsKey(key) ? uploadMap.get(key) : 0;

        if (uploadId > 0)
            AWSManager.getTransferUtility().cancel(uploadId);

        uploadMap.remove(key);
        requestMap.remove(key);
        queue.remove(image);
        cachedQueue.remove(image);
        deleteQueue.add(image);

        if (URLUtils.isLocal(image.url) &&
                new File(image.url).exists()) {
            MediaManager.getDefault().deleteFile(image, true);

            if (total > 0)
                total--;
        }
    }

    public void clearAll() {
        cancelAll();

        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                for (Page page : model.pages.data) {
                    if (page.media.id < 1)
                        MediaManager.getDefault().deleteFiles(page.media.images);
                }
            }
        });
    }

    public void continueUploading() {
        if (UploadState.Processing.equals(uploadState)) {
            if (progress < total)
                dequeue();
        } else {
            if (delegate != null)
                delegate.onStartUploading(this);

            EventBus.getDefault().post(new Event(Event.START_UPLOADING, this));

            if (!validateCompletion() && queue.size() > 0)
                startUploading();
        }
    }

    public void enqueue(final List<Page> pages) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (Page page : pages) {
                    enqueue(page.media.images.standard_resolution);
                    enqueue(page.media.images.low_resolution);
                    enqueue(page.media.images.thumbnail);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                continueUploading();
            }
        }.execute();
    }

    public void pauseAll() {
        AWSManager.getTransferUtility().pauseAllWithType(TransferType.UPLOAD);
        requestMap.clear();
        uploadMap.clear();

        uploadState = UploadState.None;
    }

    public void remove(List<Page> pages) {
        for (Page page : pages) {
            clear(page);
        }
    }

    public void retryUploading() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (Image image : cachedQueue) {
                    queue.offer(image);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                continueUploading();
            }
        }.execute();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void checkCompletion() {
        if (!UploadState.Processing.equals(uploadState))
            return;

        if (delegate != null)
            delegate.onProcessUploading(this);

        EventBus.getDefault().post(new Event(Event.PROCESS_UPLOADING, this));

        if (validateCompletion())
            return;

        startUploading();
    }

    private void dequeue() {
        if (queue.size() < 1)
            return;

        final Image image = queue.poll();
        final int key = image.hashCode();

        if (URLUtils.isLocal(image.url)) {
            int retryCount = requestMap.containsKey(key) ? requestMap.get(key) : 0;
            if (retryCount < 3) {
                requestMap.put(key, retryCount + 1);

                MediaDataProxy.getDefault().getUploadInfo(new Callback<ApiMedia.UploadInfoRes>() {
                    @Override
                    public void onResponse(Response<ApiMedia.UploadInfoRes> response, Retrofit retrofit) {
                        if (!deleteQueue.contains(image)) {
                            if (response.isSuccess() && response.body().isSuccess()) {
                                ApiMedia.UploadInfoEntity entity = response.body().entity;

                                int uploadId = MediaManager.getDefault().uploadImage(
                                        image,
                                        entity.filename,
                                        new MediaManager.CompleteHandler() {
                                            @Override
                                            public void onComplete(Error error) {
                                                cachedQueue.remove(image);
                                                uploadMap.remove(key);

                                                if (error != null) {
                                                    cachedQueue.offer(image);
                                                    queue.offer(image);
                                                }

                                                processUploading(error == null);
                                            }
                                        });

                                if (uploadId > 0)
                                    uploadMap.put(key, uploadId);
                            } else {
                                cachedQueue.remove(image);
                                cachedQueue.offer(image);
                                queue.offer(image);
                                processUploading(false);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (!deleteQueue.contains(image)) {
                            cachedQueue.remove(image);
                            cachedQueue.offer(image);
                            queue.offer(image);
                            processUploading(false);
                        }
                    }
                });
            } else {
                requestMap.clear();
                errorState();
                return;
            }
        } else {
            cachedQueue.remove(image);
            processUploading(true);
        }

        dequeue();
    }

    private void enqueue(Image image) {
        if (URLUtils.isLocal(image.url) &&
                !queue.contains(image) &&
                !deleteQueue.contains(image)) {
            queue.offer(image);
            cachedQueue.offer(image);
            total++;
        }
    }

    private void errorState() {
        AWSManager.getTransferUtility().cancelAllWithType(TransferType.UPLOAD);

        if (delegate != null)
            delegate.onFailUploading(this);

        EventBus.getDefault().post(new Event(Event.FAILED_UPLOADING, this));
        uploadState = UploadState.Failed;
    }

    private void executeCompletionState() {
        uploadState = UploadState.Complete;

        if (delegate != null)
            delegate.onCompleteUploading(this);

        EventBus.getDefault().post(new Event(Event.COMPLETE_UPLOADING, this));

        if (CompletionState.Creation.equals(completionState)) {
            requestAlbumCreation();
        } else if (CompletionState.Modification.equals(completionState)) {
            requestAlbumModification();
        }
    }

    private void processUploading(Boolean isSuccess) {
        if (!UploadState.Processing.equals(uploadState))
            return;

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
        uploadState = UploadState.Processing;

        if (progress < total)
            dequeue();
    }

    private boolean validateCompletion() {
        if (current >= total) {
            requestMap.clear();
            uploadMap.clear();
            queue.clear();
            cachedQueue.clear();
            deleteQueue.clear();
            executeCompletionState();

            current = 0;
            progress = 0;
            total = 0;
            return true;
        }
        return false;
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
