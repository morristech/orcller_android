package com.orcller.app.orcllermodules.error;

/**
 * Created by pisces on 11/26/15.
 */
public class FacebookSDKError extends APIError {
    public static final int FacebookSDKErrorCancel = 6001;
    public static final String FacebookSDKErrorMessageCancel = "Facebook Login has been canceled.";

    public FacebookSDKError(int code, String detailMessage) {
        super(code, detailMessage);
    }
}