package com.orcller.app.orcllermodules.caches;

import android.os.Bundle;

import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.facebook.FBPhoto;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pisces on 11/26/15.
 */
public class FBPhotoCaches {
    private static FBPhotoCaches uniqueInstance;
    private Map<String, FBPhoto> map = new HashMap<String, FBPhoto>();

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static FBPhotoCaches getDefault() {
        if(uniqueInstance == null) {
            synchronized(FBPhotoCaches.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new FBPhotoCaches();
                }
            }
        }
        return uniqueInstance;
    }

    public void clear() {
        map.clear();
    }

    public void getPhoto(final String id, final FBSDKRequest.CompleteHandler completeHandler) {
        if (map.containsKey(id)) {
            completeHandler.onComplete(map.get(id), null);
        } else {
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,images,created_time,updated_time");

            FBSDKRequestQueue.currentQueue().request(
                    id,
                    parameters,
                    FBPhoto.class,
                    new FBSDKRequest.CompleteHandler<FBPhoto>() {
                        @Override
                        public void onComplete(FBPhoto result, APIError error) {
                            if (error == null) {
                                map.put(id, result);
                                completeHandler.onComplete(result, null);
                            } else {
                                completeHandler.onComplete(null, error);
                            }
                        }
                    }
            );
        }
    }
}
