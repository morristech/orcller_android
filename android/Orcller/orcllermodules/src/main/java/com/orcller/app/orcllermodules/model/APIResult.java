package com.orcller.app.orcllermodules.model;

import java.util.List;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/4/15.
 */

public class ApiResult<T> extends Model {
    public enum ApiResultCode {
        Failed(0),
        Ok(1);

        private int value;

        private ApiResultCode(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public int code;
    public int error_code;
    public String code_message;
    public String error_message;
    public T entity;
    public List<T> entities;

    public boolean isSuccess() {
        return this.code == ApiResultCode.Ok.value();
    }
}