package com.orcller.app.orcller.model.api;

import com.orcller.app.orcllermodules.model.ApiResult;

/**
 * Created by pisces on 12/3/15.
 */
public class ApiMedia {
    public static class UploadInfoRes extends ApiResult {
        public UploadInfoEntity entity;
    }

    public static class UploadInfoEntity {
        public String filename;
    }
}
