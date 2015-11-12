package com.orcller.app.orcllermodules.queue;

import android.app.Activity;
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
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import com.orcller.app.orcllermodules.utils.GSonUtil;
import com.orcller.app.orcllermodules.utils.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pisces on 11/9/15.
 */
public class FBSDKRequestQueue {
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

        FBSDKRequest object = arrayList.get(0);

        if (isValidAccessToken()) {
            arrayList.remove(0);
            request(object);
        } else {
            callbackManager = CallbackManager.Factory.create();

            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    dequeue();
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onError(FacebookException error) {
                }
            });

            if (object.getTarget() instanceof Activity) {
                LoginManager.getInstance().logInWithReadPermissions((Activity) object.getTarget(), PERMISSIONS);
            } else if (object.getTarget() instanceof Fragment) {
                LoginManager.getInstance().logInWithReadPermissions((Fragment) object.getTarget(), PERMISSIONS);
            }
        }

        return object;
    }

    private boolean isValidAccessToken() {
        return AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired();
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
                            object.getCompleteHandler().onComplete(response.getJSONObject(), null);
                        } else {
                            APIError error = APIError.newInstance(
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
