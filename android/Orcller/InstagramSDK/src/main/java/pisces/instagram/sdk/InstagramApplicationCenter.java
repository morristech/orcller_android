package pisces.instagram.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Map;

import de.greenrobot.event.EventBus;
import pisces.instagram.sdk.activity.InstagramLoginActivity;
import pisces.instagram.sdk.error.InstagramSDKError;
import pisces.instagram.sdk.model.ApiInstagram;
import pisces.instagram.sdk.model.ApiInstagramResult;
import pisces.instagram.sdk.model.OAuth2;
import pisces.instagram.sdk.proxy.InstagramApiProxy;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GSonUtil;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramApplicationCenter<T> {
    private static InstagramApplicationCenter uniqueInstance;
    private static final String APP_PREFERENCE_KEY = "Application-Preferences";
    private static final String CACHED_INSTAGRAM_AUTH_INFO_KEY = "CachedInstagramAuthInfo";
    private static final String CLIENT_ID_KEY = "pisces.instagram.sdk.ClientId";
    private static final String CLIENT_SECRET_KEY = "pisces.instagram.sdk.ClientSecret";
    private static final String REDIRECT_URL_KEY = "pisces.instagram.sdk.RedirectUrl";
    private String accessToken;
    private String code;
    private ArrayList<CallCommand> commandQueue = new ArrayList<CallCommand>();
    private ApiInstagram.AccessTokenRes cachedAccessTokenRes;
    private OAuth2 resource;
    private InstagramApiProxy.CompleteHandler completeHandler;

    /**
     * @constructor
     **/
    public InstagramApplicationCenter() {
        cachedAccessTokenRes = getCachedAccessTokenRes();
        accessToken = cachedAccessTokenRes != null ? cachedAccessTokenRes.access_token : null;
        code = cachedAccessTokenRes != null ? cachedAccessTokenRes.code : null;

        try {
            Context context = Application.applicationContext();
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String clientId = bundle.getString(CLIENT_ID_KEY);
            String clientSecret = bundle.getString(CLIENT_SECRET_KEY);
            String redirectUrl = bundle.getString(REDIRECT_URL_KEY);

            resource = new OAuth2.Builder().setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRedirectURI(redirectUrl)
                    .setScope(OAuth2.InstagramLoginScope.Basic).build();
        } catch (PackageManager.NameNotFoundException e) {
           e.printStackTrace();
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static InstagramApplicationCenter getDefault() {
        if(uniqueInstance == null) {
            synchronized(InstagramApplicationCenter.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new InstagramApplicationCenter();
                }
            }
        }
        return uniqueInstance;
    }

    public CallCommand enqueueCall(
            Call<ApiInstagramResult> call,
            InstagramApiProxy.CompleteHandler completeHandler) {
        CallCommand command = new CallCommand(call, completeHandler);
        commandQueue.add(command);
        dequeueCommand();
        return command;
    }

    public boolean hasSession() {
        return accessToken != null;
    }

    public void login(InstagramApiProxy.CompleteHandler completeHandler) {
        if (invalidateResource(completeHandler))
            return;

        if (code != null) {
            refresh(completeHandler);
        } else {
            this.completeHandler = completeHandler;

            EventBus.getDefault().register(this);

            Intent intent = new Intent(Application.applicationContext(), InstagramLoginActivity.class);
            intent.putExtra("resource", resource);
            Application.applicationContext().startActivity(intent);
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getCode() {
        return code;
    }

    public OAuth2 getResource() {
        return resource;
    }

    public void setResource(OAuth2 resource) {
        this.resource = resource;
    }

    // ================================================================================================
    //  Event Handler
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof InstagramLoginActivity.InstagramLoginComplete) {
            EventBus.getDefault().unregister(this);

            InstagramLoginActivity.InstagramLoginComplete casted = (InstagramLoginActivity.InstagramLoginComplete) event;
            casted.getActivity().finish();
            code = casted.getCode();

            if (code != null) {
                requestAccessToken(completeHandler);
                completeHandler = null;
            }
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void dequeueCommand() {
        if (commandQueue.size() < 1)
            return;

        final CallCommand command = commandQueue.get(0);

        if (hasSession()) {
            executeCommand(command);
        } else {
            login(new InstagramApiProxy.CompleteHandler() {
                @Override
                public void onError(InstagramSDKError error) {
                    commandQueue.remove(command);
                    command.completeHandler.onError(error);
                }

                @Override
                public void onComplete(ApiInstagramResult result) {
                    executeCommand(command);
                }
            });
        }
    }

    private void executeCommand(final CallCommand command) {
        if (command != null) {
            commandQueue.remove(command);

            InstagramApiProxy.getDefault().enqueueCall(command.getCall(), new Callback<ApiInstagramResult>() {
                @Override
                public void onResponse(Response<ApiInstagramResult> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        command.getCompleteHandler().onComplete(response.body());
                    } else {
                        command.getCompleteHandler().onError(new InstagramSDKError(
                                InstagramSDKError.Code.UnknownAPIError.getValue(),
                                response.message()));
                    }
                    command.clear();
                }

                @Override
                public void onFailure(Throwable t) {
                    command.getCompleteHandler().onError(new InstagramSDKError(
                            InstagramSDKError.Code.UnknownAPIError.getValue(), t.getMessage()));
                    command.clear();
                }
            });
        }
    }

    private boolean invalidateCode(InstagramApiProxy.CompleteHandler completeHandler) {
        if (code != null)
            return false;

        completeHandler.onError(new InstagramSDKError(
                InstagramSDKError.Code.NeedAuthorizationError,
                InstagramSDKError.Message.NeedAuthorizationError));
        return true;
    }

    private boolean invalidateResource(InstagramApiProxy.CompleteHandler completeHandler) {
        InstagramSDKError error = null;

        if (resource == null) {
            error = new InstagramSDKError(
                    InstagramSDKError.Code.DoesNotExistResourceError,
                    InstagramSDKError.Message.DoesNotExistResourceError);
        }

        if (resource.getClientId() == null) {
            error = new InstagramSDKError(
                    InstagramSDKError.Code.InvalidClientIdError,
                    InstagramSDKError.Message.InvalidClientIdError);
        }

        if (resource.getClientSecret() == null) {
            error = new InstagramSDKError(
                    InstagramSDKError.Code.InvalidClientSecretError,
                    InstagramSDKError.Message.InvalidClientSecretError);
        }

        if (resource.getRedirectURI() == null) {
            error = new InstagramSDKError(
                    InstagramSDKError.Code.InvalidRedirectURIError,
                    InstagramSDKError.Message.InvalidRedirectURIError);
        }

        if (error != null) {
            completeHandler.onError(error);
            return true;
        }
        return false;
    }

    private void refresh(InstagramApiProxy.CompleteHandler completeHandler) {
        if (!invalidateCode(completeHandler))
            requestAccessToken(completeHandler);
    }

    private void requestAccessToken(final InstagramApiProxy.CompleteHandler completeHandler) {
        if (invalidateCode(completeHandler))
            return;

        InstagramApiProxy.Service service = (InstagramApiProxy.Service) InstagramApiProxy.getDefault().getCurrentService();
        Map <String, String> parameters = resource.getParameters(OAuth2.InstagramParametersType.AccessToken);
        Call<ApiInstagram.AccessTokenRes> call = service.accessToken(parameters);

        final InstagramApiProxy.CompleteHandler handler = new InstagramApiProxy.CompleteHandler() {
            @Override
            public void onError(InstagramSDKError error) {
                if (completeHandler != null)
                    completeHandler.onError(error);
            }

            @Override
            public void onComplete(ApiInstagramResult result) {
                if (completeHandler != null)
                    completeHandler.onComplete(result);
            }
        };

        InstagramApiProxy.getDefault().enqueueCall(call, new Callback<ApiInstagram.AccessTokenRes>() {
            @Override
            public void onResponse(Response<ApiInstagram.AccessTokenRes> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ApiInstagram.AccessTokenRes result = response.body();
                    accessToken = result.access_token;

                    setCachedAccessTokenRes(result);
                    handler.onComplete(result);
                } else {
                    handler.onError(new InstagramSDKError(
                            InstagramSDKError.Code.UnknownAPIError.getValue(),
                            response.message()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                handler.onError(new InstagramSDKError(
                        InstagramSDKError.Code.UnknownAPIError.getValue(), t.getMessage()));
            }
        });
    }

    private ApiInstagram.AccessTokenRes getCachedAccessTokenRes() {
        String cachedUser = getSharedPreference().getString(CACHED_INSTAGRAM_AUTH_INFO_KEY, null);
        if (cachedUser != null)
            return (ApiInstagram.AccessTokenRes) GSonUtil.objectFromGSonString(cachedUser, ApiInstagram.AccessTokenRes.class);
        return null;
    }

    private void setCachedAccessTokenRes(ApiInstagram.AccessTokenRes authInfo) {
        SharedPreferences.Editor editor = getSharedPreference().edit();

        if (authInfo != null)
            editor.putString(CACHED_INSTAGRAM_AUTH_INFO_KEY, GSonUtil.toGSonString(authInfo));
        else
            editor.remove(CACHED_INSTAGRAM_AUTH_INFO_KEY);

        editor.commit();
    }

    private SharedPreferences getSharedPreference() {
        return Application.applicationContext()
                .getSharedPreferences(APP_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    // ================================================================================================
    //  Class: CallCommand
    // ================================================================================================

    private class CallCommand {
        private Call<ApiInstagramResult> call;
        private InstagramApiProxy.CompleteHandler completeHandler;

        public CallCommand(
                Call<ApiInstagramResult> call,
                InstagramApiProxy.CompleteHandler completeHandler) {
            this.call = call;
            this.completeHandler = completeHandler;
        }

        public void clear() {
            call = null;
            completeHandler = null;
        }

        public Call<ApiInstagramResult> getCall() {
            return call;
        }

        public InstagramApiProxy.CompleteHandler getCompleteHandler() {
            return completeHandler;
        }
    }
}
