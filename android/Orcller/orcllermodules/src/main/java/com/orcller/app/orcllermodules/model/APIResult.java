package com.orcller.app.orcllermodules.model;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/4/15.
 */

public class ApiResult extends Model {
    public enum APIResultCode {
        APIResultCodeFailed(0),
        APIResultCodeOk(1);

        private int value;

        private APIResultCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public int code;
    public int error_code;
    public String code_message;
    public String error_message;

    public boolean isSuccess() {
        return this.code == APIResultCode.APIResultCodeOk.getValue();
    }
}