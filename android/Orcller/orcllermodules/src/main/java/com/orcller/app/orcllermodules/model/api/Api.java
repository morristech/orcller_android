package com.orcller.app.orcllermodules.model.api;

import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.APIResult;

/**
 * Created by pisces on 11/10/15.
 */
public class Api {
    public interface CompleteHandler {
        void onComplete(Object result, APIError error);
    }
}
