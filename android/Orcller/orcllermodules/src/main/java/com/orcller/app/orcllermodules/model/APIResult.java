package com.orcller.app.orcllermodules.model;

import com.google.gson.Gson;

/**
 * Created by pisces on 11/4/15.
 */

public class APIResult {
    public enum APIResultCode {
        APIResultCodeFailed,
        APIResultCodeOk
    }

    public APIResultCode code;
    public int error_code;
    public String code_message;
    public String error_message;

    public boolean isSuccess() {
        return this.code == APIResultCode.APIResultCodeOk;
    }
}