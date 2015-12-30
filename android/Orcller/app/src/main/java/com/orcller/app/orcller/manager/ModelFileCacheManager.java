package com.orcller.app.orcller.manager;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.common.SharedObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 12/26/15.
 */
public class ModelFileCacheManager {
    private static final File CACHED_TIMELINE = new File(SharedObject.CACHE_DIR, "timeline.caches");

    public enum Type {
        Timeline
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static  <T> T load(Type type, T defaultValue) {
        final File file = getCacheFile(type);

        if (file != null && file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream is = new ObjectInputStream(fis);
                T result = (T) is.readObject();
                is.close();
                fis.close();

                if (result instanceof List)
                    return ((List) result).size() > 0 ? result : defaultValue;

                return result != null ? result : defaultValue;
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    public static void save(Type type, final Object object) {
        final File file = getCacheFile(type);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(object);
            os.close();
            fos.close();
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(e.getMessage());
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private static File getCacheFile(Type type) {
        if (Type.Timeline.equals(type))
            return CACHED_TIMELINE;
        return null;
    }
}
