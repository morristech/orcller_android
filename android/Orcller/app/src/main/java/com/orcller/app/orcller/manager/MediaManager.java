package com.orcller.app.orcller.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Image;
import com.orcller.app.orcller.model.Images;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.model.api.ApiMedia;
import com.orcller.app.orcller.proxy.MediaDataProxy;
import com.orcller.app.orcllermodules.error.APIError;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.BitmapUtils;
import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.URLUtils;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/16/15.
 */
public class MediaManager {
    public static final float IMAGE_LIMIT_RATE = 2.0f;
    public static final int WIDTH_IMAGE_LOW_RESOLUTION = 320;
    public static final int WIDTH_IMAGE_STANDARD_RESOLUTION = 640;
    public static final int WIDTH_IMAGE_THUMBNAIL= 150;
    private static final File CACHED_UPLOAD_UNIT_MAP = new File(SharedObject.DATA_DIR, "mediaupload.units");
    private static final File CACHED_ERROR_IMAGE_LIST = new File(SharedObject.DATA_DIR, "mediauploaderror.images");
    private static MediaManager uniqueInstance;
    private ArrayList<Image> cachedErrorImageList;
    private ArrayList<MediaUploadUnit> cachedUploadUnits;
    private HashMap<String, MediaUploadUnit> cachedUploadUnitMap;

    public MediaManager() {
        if (!SharedObject.DATA_DIR.exists())
            SharedObject.DATA_DIR.mkdir();

        cachedUploadUnitMap = loadCachedUploadUnitMap();
        cachedErrorImageList = loadCachedErrorImageList();
        cachedUploadUnits = new ArrayList<>(cachedUploadUnitMap.values());
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static MediaManager getDefault() {
        if(uniqueInstance == null) {
            synchronized(MediaManager.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new MediaManager();
                }
            }
        }
        return uniqueInstance;
    }

    public void clearAll() {
        Application.run(new Runnable() {
            @Override
            public void run() {
                for (MediaUploadUnit unit : cachedUploadUnits) {
                    unit.clearAll();
                }

                cachedUploadUnits.clear();
                cachedUploadUnitMap.clear();
            }
        }, new Runnable() {
            @Override
            public void run() {
                saveCacheFile();
            }
        });
    }

    public void clearUnnecessaryItems() {
        Application.run(new Runnable() {
            @Override
            public void run() {
                for (MediaUploadUnit unit : cachedUploadUnits) {
                    if (unit.getCompletionState().equals(MediaUploadUnit.CompletionState.None)) {
                        unit.clearAll();
                    }
                }

                cachedUploadUnits.clear();
                cachedUploadUnitMap.clear();

                for (Image image : cachedErrorImageList) {
                    deleteFile(image, false);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                saveCacheFile();
            }
        });
    }

    public void completeUploading(final Album model) {
        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                String key = String.valueOf(model.id);
                MediaUploadUnit unit = cachedUploadUnitMap.get(key);
                if (unit != null) {
                    cachedUploadUnits.remove(unit);
                    cachedUploadUnitMap.remove(key);
                    saveCacheFile();
                }
            }
        });
    }

    public void continueUploading() {
        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                for (MediaUploadUnit unit : cachedUploadUnits) {
                    unit.upload();
                }
            }
        });
    }

    public void deleteFiles(Images images) {
        deleteFile(images.standard_resolution, true);
        deleteFile(images.low_resolution, true);
        deleteFile(images.thumbnail, true);
    }

    public List<MediaUploadUnit> getCachedUploadUnits() {
        return cachedUploadUnits;
    }

    public MediaUploadUnit getUnit(Album model) {
        String key = String.valueOf(model.id);
        MediaUploadUnit unit = cachedUploadUnitMap.get(key);
        if (unit == null) {
            unit = new MediaUploadUnit(model);
            cachedUploadUnitMap.put(key, unit);
            cachedUploadUnits.add(unit);
            saveCacheFile();
        }
        return unit;
    }

    public void saveCacheErrorFile() {
        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                if (cachedErrorImageList.size() > 0) {
                    try {
                        FileOutputStream fos = new FileOutputStream(CACHED_ERROR_IMAGE_LIST);
                        ObjectOutputStream os = new ObjectOutputStream(fos);
                        os.writeObject(cachedErrorImageList);
                        os.close();
                        fos.close();
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG)
                            Log.e("saveCacheErrorFile Error", e.getMessage());
                    }
                } else if (CACHED_ERROR_IMAGE_LIST.exists()) {
                    CACHED_ERROR_IMAGE_LIST.delete();
                }
            }
        });
    }

    public void saveCacheFile() {
        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                if (cachedUploadUnitMap.size() > 0) {
                    try {
                        FileOutputStream fos = new FileOutputStream(CACHED_UPLOAD_UNIT_MAP);
                        ObjectOutputStream os = new ObjectOutputStream(fos);
                        os.writeObject(cachedUploadUnitMap);
                        os.close();
                        fos.close();
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG)
                            Log.e("saveCacheFile Error", e);
                    }
                } else if (CACHED_UPLOAD_UNIT_MAP.exists()) {
                    CACHED_UPLOAD_UNIT_MAP.delete();
                }
            }
        });
    }

    public void saveToTemp(final pisces.psuikit.imagepicker.Media source, final Media media, final CompleteHandler completeHandler) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeFile(source.path, options);

                saveTempImages(bitmap, media);

                if (media.isVideo()) {
                    //TODO: Impl Next Version
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                completeHandler.onComplete(null);
            }
        }.execute();
    }

    public void saveUserPicture(Bitmap source, final String filename, final CompleteHandler completeHandler) {
        final List<SharedObject.SizeType> types = Arrays.asList
                (SharedObject.SizeType.Large, SharedObject.SizeType.Medium, SharedObject.SizeType.Small);
        final Point p = new Point(0, types.size());

        CompleteHandler checkComplete = new CompleteHandler() {
            @Override
            public void onComplete(Error error) {
                if (p.x >= p.y)
                    return;

                if (error != null) {
                    AWSManager.getTransferUtility().cancelAllWithType(TransferType.UPLOAD);

                    for (SharedObject.SizeType type : types) {
                        File file = new File(SharedObject.TEMP_IMAGE_DIR, SharedObject.extractFilename(filename, type));

                        if (file.exists())
                            file.delete();
                    }

                    completeHandler.onComplete(error);
                    p.x = p.y;
                } else {
                    if (++p.x >= p.y) {
                        completeHandler.onComplete(null);
                    }
                }
            }
        };

        for (SharedObject.SizeType type : types) {
            uploadUserPicture(source, filename, type, checkComplete);
        }
    }

    public void startUploading(Album model) {
        getUnit(model).upload();
    }

    public void uploadImage(final Image image, String filename, final CompleteHandler completeHandler) {
        if (image == null) {
            completeHandler.onComplete(new Error());
            return;
        }

        final String key = SharedObject.getImageUploadPath(filename, new Point(image.width, image.height));
        final File file = new File(image.url);
        TransferObserver observer = AWSManager.getTransferUtility().upload(AWSManager.S3_BUCKET_NAME, key, file);

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    image.url = key;

                    if (file.exists() && !file.delete())
                        addErrorImage(image);

                    completeHandler.onComplete(null);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            }

            @Override
            public void onError(int id, Exception ex) {
                if (BuildConfig.DEBUG)
                    Log.e("uploadImage onError", key, ex);

                completeHandler.onComplete(new Error(ex.getMessage()));
            }
        });
    }

    public void uploadImageDirectly(Bitmap bitmap, Callback<ApiMedia.UploadInfoRes> callback) {
        MediaDataProxy.getDefault().uploadDirectly(bitmap, callback);
    }

    public void uploadShareImage(Bitmap bitmap, final UploadCompleteHandler completeHandler) {
        final String localFilename = String.valueOf(DateUtil.toUnixtimestamp(new Date())) + ".jpg";

        saveBitmap(bitmap, localFilename);

        MediaDataProxy.getDefault().getUploadInfo(new Callback<ApiMedia.UploadInfoRes>() {
            @Override
            public void onResponse(Response<ApiMedia.UploadInfoRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    ApiMedia.UploadInfoEntity entity = response.body().entity;
                    final String key = SharedObject.getImageUploadPath(entity.filename);
                    final File file = new File(SharedObject.TEMP_IMAGE_DIR, localFilename);

                    TransferObserver observer = AWSManager.getTransferUtility().upload(AWSManager.S3_BUCKET_NAME, key, file);
                    observer.setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            if (state == TransferState.COMPLETED) {
                                if (file.exists())
                                    file.delete();

                                completeHandler.onComplete(SharedObject.toFullMediaUrl(key), null);
                            }
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            if (BuildConfig.DEBUG)
                                Log.e("uploadShareImage Error", ex);

                            if (file.exists())
                                file.delete();

                            completeHandler.onComplete(null, new Error(ex.getMessage()));
                        }
                    });
                } else {
                    completeHandler.onComplete(null, new APIError(response.body()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                completeHandler.onComplete(null, new Error(t.getMessage()));
            }
        });
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void addErrorImage(final Image image) {
        if (image != null && !cachedErrorImageList.contains(image)) {
            cachedErrorImageList.add(image);
            saveCacheErrorFile();
        }
    }

    private void deleteFile(Image image, boolean allowsEnqueueError) {
        if (URLUtils.isLocal(image.url)) {
            File file = new File(image.url);

            if (file.exists()) {
                if (file.delete()) {
                    removeErrorImage(image);
                } else if (allowsEnqueueError) {
                    addErrorImage(image);
                }
            }
        } else {
            try {
                DeleteObjectRequest objectRequest = new DeleteObjectRequest(AWSManager.S3_BUCKET_NAME, image.url);
                AWSManager.getS3Client().deleteObject(objectRequest);
                removeErrorImage(image);
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    Log.e("deleteFile Error", e.getMessage());

                if (allowsEnqueueError)
                    addErrorImage(image);
            }
        }
    }

    private Point getFitedSize(int fitValue, Point originSize) {
        float scale = (float) fitValue / Math.min(originSize.x, originSize.y);
        int w = Math.round(originSize.x * scale);
        int h = Math.round(originSize.y * scale);

        if (w != h) {
            float rate = (float) Math.min(w, h) / Math.max(w, h);
            if (rate > IMAGE_LIMIT_RATE) {
                if (Math.max(w, h) == w) {
                    w = Math.round(h * IMAGE_LIMIT_RATE);
                } else {
                    h = Math.round(w * IMAGE_LIMIT_RATE);
                }
            }
        }

        return new Point(w, h);
    }

    private HashMap<String, MediaUploadUnit> loadCachedUploadUnitMap() {
        if (CACHED_UPLOAD_UNIT_MAP.exists()) {
            try {
                FileInputStream fis = new FileInputStream(CACHED_UPLOAD_UNIT_MAP);
                ObjectInputStream is = new ObjectInputStream(fis);
                HashMap<String, MediaUploadUnit> result = (HashMap<String, MediaUploadUnit>) is.readObject();
                is.close();
                fis.close();
                return result;
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    Log.e("loadCachedUploadUnitMap Error", e);
            }
        }
        return new HashMap<>();
    }

    private ArrayList<Image> loadCachedErrorImageList() {
        if (CACHED_ERROR_IMAGE_LIST.exists()) {
            try {
                FileInputStream fis = new FileInputStream(CACHED_ERROR_IMAGE_LIST);
                ObjectInputStream is = new ObjectInputStream(fis);
                ArrayList<Image> result = (ArrayList<Image>) is.readObject();
                is.close();
                fis.close();
                return result;
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    Log.e("loadCachedErrorImageList Error", e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    private void removeErrorImage(final Image image) {
        if (cachedErrorImageList.contains(image)) {
            cachedErrorImageList.remove(image);
            saveCacheErrorFile();
        }
    }

    private void saveBitmap(Bitmap bitmap, String filename) {
        if (!SharedObject.TEMP_IMAGE_DIR.exists())
            SharedObject.TEMP_IMAGE_DIR.mkdir();

        File file = new File(SharedObject.TEMP_IMAGE_DIR, filename);

        try {
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            bitmap.recycle();
        } catch (FileNotFoundException e) {
            if (BuildConfig.DEBUG)
                Log.e("saveBitmap Error", e.getMessage());
        }
    }

    private void saveTempImages(Bitmap bitmap, Media media) {
        String basePath = SharedObject.TEMP_IMAGE_DIR.getAbsolutePath();
        Point originSize = new Point(bitmap.getWidth(), bitmap.getHeight());
        Point standardSize = getFitedSize(WIDTH_IMAGE_STANDARD_RESOLUTION, originSize);
        Point lowSize = getFitedSize(WIDTH_IMAGE_LOW_RESOLUTION, originSize);
        Point thumbnailSize = getFitedSize(WIDTH_IMAGE_THUMBNAIL, originSize);
        Bitmap standardBitmap = BitmapUtils.createScaledBitmap(standardSize, bitmap);
        Bitmap lowBitmap = BitmapUtils.createScaledBitmap(lowSize, bitmap);
        Bitmap thumbnailBitmap = BitmapUtils.createScaledBitmap(thumbnailSize, bitmap);
        String standardImageName = String.valueOf(media.origin_id) + "_s.jpg";
        String lowImageName = String.valueOf(media.origin_id) + "_l.jpg";
        String thumbnailImageName = String.valueOf(media.origin_id) + "_t.jpg";

        saveBitmap(standardBitmap, standardImageName);
        saveBitmap(lowBitmap, lowImageName);
        saveBitmap(thumbnailBitmap, thumbnailImageName);

        media.images = new Images();
        media.images.low_resolution = new Image(lowBitmap.getWidth(), lowBitmap.getHeight(), basePath + "/" + lowImageName);
        media.images.standard_resolution = new Image(standardBitmap.getWidth(), standardBitmap.getHeight(), basePath + "/" + standardImageName);
        media.images.thumbnail = new Image(thumbnailBitmap.getWidth(), thumbnailBitmap.getHeight(), basePath + "/" + thumbnailImageName);
    }

    private void uploadUserPicture(final Bitmap source, final String filename,
                                   final SharedObject.SizeType sizeType, final CompleteHandler completeHandler) {
        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                int pixel = SharedObject.convertSizeTypeToPixel(sizeType);
                float scale = (float) pixel / source.getWidth();
                int w = Math.min(Math.round(source.getWidth() * scale), source.getWidth());
                int h = Math.min(Math.round(source.getHeight() * scale), source.getHeight());
                Bitmap bitmap = Bitmap.createScaledBitmap(source, w, h, true);
                String path = SharedObject.extractFilename(filename, sizeType);
                final String key = SharedObject.toUserPictureUrl(filename, sizeType, true);
                final File file = new File(SharedObject.TEMP_IMAGE_DIR, path);

                saveBitmap(bitmap, path);

                Application.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        TransferObserver observer = AWSManager.getTransferUtility().upload(AWSManager.S3_BUCKET_NAME, key, file);
                        observer.setTransferListener(new TransferListener() {
                            @Override
                            public void onStateChanged(int id, TransferState state) {
                                if (state == TransferState.COMPLETED) {
                                    if (file.exists())
                                        file.delete();

                                    completeHandler.onComplete(null);
                                }
                            }

                            @Override
                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                if (BuildConfig.DEBUG)
                                    Log.e("uploadUserPicture onError", ex);

                                completeHandler.onComplete(new Error(ex.getMessage()));
                            }
                        });
                    }
                });
            }
        });
    }

    // ================================================================================================
    //  Interface
    // ================================================================================================

    public interface CompleteHandler {
        void onComplete(Error error);
    }

    public interface UploadCompleteHandler {
        void onComplete(String result, Error error);
    }
}
