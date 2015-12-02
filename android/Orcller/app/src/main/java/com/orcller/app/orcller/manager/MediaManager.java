package com.orcller.app.orcller.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Image;
import com.orcller.app.orcller.model.album.Images;
import com.orcller.app.orcller.model.album.Media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.URLUtils;

/**
 * Created by pisces on 11/16/15.
 */
public class MediaManager {
    private static final int WIDTH_IMAGE_LOW_RESOLUTION = 320;
    private static final int WIDTH_IMAGE_STANDARD_RESOLUTION = 640;
    private static final int WIDTH_IMAGE_THUMBNAIL= 150;
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
                        cachedUploadUnits.remove(unit);
                        cachedUploadUnitMap.remove(String.valueOf(unit.getModel().id));
                    }
                }

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

    public boolean clearUploading(Album model) {
        String key = String.valueOf(model.id);
        MediaUploadUnit unit = cachedUploadUnitMap.get(key);
        if (unit != null) {
            unit.cancelAll();
            cachedUploadUnits.remove(unit);
            cachedUploadUnitMap.remove(key);
            saveCacheFile();
            return true;
        }
        return false;
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
                            Log.e(e.getMessage());
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
                if (cachedUploadUnits.size() > 0) {
                    try {
                        FileOutputStream fos = new FileOutputStream(CACHED_UPLOAD_UNIT_MAP);
                        ObjectOutputStream os = new ObjectOutputStream(fos);
                        os.writeObject(cachedUploadUnits);
                        os.close();
                        fos.close();
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG)
                            Log.e(e.getMessage(), e);
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

    public void startUploading(Album model) {
        getUnit(model).upload();
    }

    public void uploadImage(final Image image, String filename, final CompleteHandler completeHandler) {
        if (image == null) {
            completeHandler.onComplete(new Error());
            return;
        }

        String url = image.url;
        final String key = SharedObject.getImageUploadPath(filename, new Point(image.width, image.height));
        final File file = new File(url);

        TransferObserver observer = AWSManager.getTransferUtility().upload(AWSManager.S3_BUCKET_NAME, key, file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    Log.d("Upload complete", key);
                    image.url = key;

                    if (file.exists() && !file.delete()) {
                        addErrorImage(image);
                    }

                    completeHandler.onComplete(null);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("Upload failed", key);
                completeHandler.onComplete(new Error(ex.getMessage()));
            }
        });
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void addErrorImage(final Image image) {
        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                if (image != null && !cachedErrorImageList.contains(image)) {
                    cachedErrorImageList.add(image);
                    saveCacheErrorFile();
                }
            }
        });
    }

    private Bitmap createBitmap(Point size, Bitmap source) {
        int limitValue = Math.min(size.x, size.y);
        int w = Math.min(limitValue, size.x);
        int h = Math.min(limitValue, size.y);
        int x = Math.max(0, (size.x - w) / 2);
        int y = Math.max(0, (size.y - h)/2);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, size.x, size.y, true);
        Bitmap bitmap = Bitmap.createBitmap(scaledBitmap, x, y, w, h);
        scaledBitmap.recycle();
        return bitmap;
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
                    Log.e(e.getMessage());

                if (allowsEnqueueError)
                    addErrorImage(image);
            }
        }
    }

    private Point getFitedSize(int fitValue, Point originSize) {
        int minValue = Math.min(originSize.x, originSize.y);
        float scale = (float) Math.min(minValue, fitValue) / Math.max(minValue, fitValue);
        int w = Math.round(originSize.x * scale);
        int h = Math.round(originSize.y * scale);
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
                    Log.e(e.getMessage());
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
                    Log.e(e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    private void removeErrorImage(final Image image) {
        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                if (cachedErrorImageList.contains(image)) {
                    cachedErrorImageList.remove(image);
                    saveCacheErrorFile();
                }
            }
        });
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
                Log.e(e.getMessage());
        }
    }

    private void saveTempImages(Bitmap bitmap, Media media) {
        String basePath = SharedObject.TEMP_IMAGE_DIR.getAbsolutePath();
        Point originSize = new Point(bitmap.getWidth(), bitmap.getHeight());
        Point standardSize = getFitedSize(WIDTH_IMAGE_STANDARD_RESOLUTION, originSize);
        Point lowSize = getFitedSize(WIDTH_IMAGE_LOW_RESOLUTION, originSize);
        Point thumbnailSize = getFitedSize(WIDTH_IMAGE_THUMBNAIL, originSize);
        Bitmap standardBitmap = createBitmap(standardSize, bitmap);
        Bitmap lowBitmap = createBitmap(lowSize, bitmap);
        Bitmap thumbnailBitmap = createBitmap(thumbnailSize, bitmap);
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

    // ================================================================================================
    //  Interface
    // ================================================================================================

    public static interface CompleteHandler {
        void onComplete(Error error);
    }
}
