package com.orcller.app.orcllermodules.queue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.orcller.app.orcllermodules.BuildConfig;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.error.FacebookSDKError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GsonUtil;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/9/15.
 */
public class FBSDKRequestQueue<T> {
    public static final List<String> PERMISSIONS = Arrays.asList("user_photos", "user_videos", "user_friends");

    private static FBSDKRequestQueue uniqueInstance;

    private ArrayList<FBSDKRequest> arrayList;
    private CallbackManager callbackManager;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public FBSDKRequestQueue() {
        arrayList = new ArrayList<FBSDKRequest>();
    }

    public static FBSDKRequestQueue currentQueue() {
        if(uniqueInstance == null) {
            synchronized(FBSDKRequestQueue.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new FBSDKRequestQueue();
                }
            }
        }
        return uniqueInstance;
    }

    public void clear() {
        for (FBSDKRequest request : arrayList) {
            request.clear();
        }

        arrayList.clear();
    }

    public CallbackManager getCallbackManager() {
        return callbackManager;
    }

    public boolean isValidAccessToken() {
        return AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager != null)
            return callbackManager.onActivityResult(requestCode, resultCode, data);
        return false;
    }

    public FBSDKRequest request(
            String graphPath,
            Bundle parameters,
            Class mappingClass,
            FBSDKRequest.CompleteHandler completeHandler) {
        enqueue(null, graphPath, parameters, mappingClass, completeHandler);
        return dequeue();
    }

    public FBSDKRequest request(
            Object target, String graphPath,
            Bundle parameters,
            Class mappingClass,
            FBSDKRequest.CompleteHandler completeHandler) {
        enqueue(target, graphPath, parameters, mappingClass, completeHandler);
        return dequeue();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private FBSDKRequest enqueue(
            Object target,
            String graphPath,
            Bundle parameters,
            Class mappingClass,
            FBSDKRequest.CompleteHandler completeHandler) {
        FBSDKRequest request = new FBSDKRequest.Builder()
                .setTarget(target)
                .setGraphPath(graphPath)
                .setParameters(parameters)
                .setMappingClass(mappingClass)
                .setCompleteHandler(completeHandler)
                .build();
        arrayList.add(request);
        return request;
    }

    private FBSDKRequest dequeue() {
        if (arrayList.size() < 1)
            return null;

        final FBSDKRequest object = arrayList.get(0);

        if (isValidAccessToken()) {
            arrayList.remove(0);
            request(object);
        } else {
            callbackManager = CallbackManager.Factory.create();

            final FBSDKRequest.CompleteHandler handler = new FBSDKRequest.CompleteHandler<JSONObject>() {
                @Override
                public void onComplete(JSONObject result, APIError error) {
                    if (object.getCompleteHandler() != null)
                        object.getCompleteHandler().onComplete(result, error);
                }
            };

            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    dequeue();
                }

                @Override
                public void onCancel() {
                    arrayList.remove(0);
                    APIError err = FacebookSDKError.create(
                            FacebookSDKError.FacebookSDKErrorCancel,
                            FacebookSDKError.FacebookSDKErrorMessageCancel);
                    handler.onComplete(null, err);
                }

                @Override
                public void onError(FacebookException error) {
                    if (BuildConfig.DEBUG)
                        Log.e(error.getMessage(), error);

                    arrayList.remove(0);
                    APIError err = APIError.create(
                            APIError.APIErrorCodeUnknown,
                            error.getMessage());
                    handler.onComplete(null, err);
                }
            });

            if (object.getTarget() instanceof Activity) {
                LoginManager.getInstance().logInWithReadPermissions((Activity) object.getTarget(), PERMISSIONS);
            } else if (object.getTarget() instanceof Fragment) {
                LoginManager.getInstance().logInWithReadPermissions((Fragment) object.getTarget(), PERMISSIONS);
            } else {
                LoginManager.getInstance().logInWithReadPermissions(Application.getTopActivity(), PERMISSIONS);
            }
        }

        return object;
    }

    private void request(final FBSDKRequest object) {
        if (!isValidAccessToken())
            return;

        GraphRequest graphRequest = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                object.getGraphPath(),
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() == null) {
                            if (object.getMappingClass() != null) {
                                object.getCompleteHandler().onComplete(
                                        GsonUtil.fromJson(response.getJSONObject().toString(), object.getMappingClass()),
                                        null);
                            } else {
                                object.getCompleteHandler().onComplete(response.getJSONObject(), null);
                            }
                        } else {
                            APIError error = APIError.create(
                                    response.getError().getErrorCode(),
                                    response.getError().getErrorMessage());
                            object.getCompleteHandler().onComplete(null, error);
                        }
                    }
        });

        graphRequest.setParameters(object.getParameters());
        graphRequest.executeAsync();
    }
}