package com.orcller.app.orcller.model.api;

import com.orcller.app.orcllermodules.model.APIResult;

/**
 * Created by pisces on 12/3/15.
 */
public class ApiMedia {
    public static class UploadInfoRes extends APIResult {
        public UploadInfoEntity entity;
    }

    public static class UploadInfoEntity {
        public String filename;
    }
}
