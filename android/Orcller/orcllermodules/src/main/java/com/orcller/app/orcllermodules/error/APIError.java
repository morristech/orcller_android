package com.orcller.app.orcllermodules.error;

import android.os.Bundle;

import com.orcller.app.orcllermodules.model.APIResult;

/**
 * Created by pisces on 11/5/15.
 */
public class APIError extends Error {
    public static final int APIErrorCodeUnknown = 5001;
    public static final int APIErrorCodeUserDoesNotExist = 5002;
    public static final int APIErrorCodeUserDoesExist = 5003;
    public static final int APIErrorCodeInvalidParameters = 5004;
    public static final int APIErrorCodeApplicationDoesNotMatch = 5005;
    public static final int APIErrorCodeUserPasswordsDoNotMatch = 5006;
    public static final int APIErrorCodeInvalidSessionToken = 5007;
    public static final int APIErrorCodeNoPermissionForAlbum = 5008;
    public static final int APIErrorCodeAlbumDoesNotExist = 5009;
    public static final int APIErrorCodeMySQLDBError = 5010;
    public static final int APIErrorCodeFollowingIsAlready = 5011;
    public static final int APIErrorCodeUserDoesNotFollower = 5012;
    public static final int APIErrorCodeTheWrongTarget = 5013;
    public static final int APIErrorCodePageDoesNotExist = 5014;
    public static final int APIErrorCodeEmailAuthenticationMailSendFailure = 5015;
    public static final int APIErrorCodeEmailAuthenticationFailure = 5015;

    private int code;

    public APIError(int code, String detailMessage) {
        super(detailMessage);

        this.code = code;
    }

    public APIError(APIResult result) {
        super(result.error_message);

        this.code = result.error_code;
    }

    public static APIError newInstance(APIResult result) {
        return new APIError(result);
    }

    public static APIError newInstance(int code, String detailMessage) {
        return new APIError(code, detailMessage);
    }

    public int getCode() {
        return code;
    }
}
