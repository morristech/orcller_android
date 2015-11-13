package pisces.instagram.sdk;

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
import pisces.instagram.sdk.model.InstagramAuth;
import pisces.instagram.sdk.model.OAuth2;
import pisces.instagram.sdk.proxy.InstagramApiProxy;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.AbstractModel;
import pisces.psfoundation.utils.GSonUtil;
import pisces.psfoundation.utils.Log;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramApplicationCenter {
    private static InstagramApplicationCenter uniqueInstance;
    private static final String APP_PREFERENCE_KEY = "Application-Preferences";
    private static final String CACHED_INSTAGRAM_AUTH_INFO = "kCachedInstagramAuthInfo";
    private static final String CLIENT_ID_KEY = "pisces.instagram.sdk.ClientId";
    private static final String CLIENT_SECRET_KEY = "pisces.instagram.sdk.ClientSecret";
    private static final String REDIRECT_URL_KEY = "pisces.instagram.sdk.RedirectUrl";
    private String accessToken;
    private String code;
    private ArrayList<CallCommand> commandQueue = new ArrayList<CallCommand>();
    private InstagramAuth cachedAuthInfo;
    private OAuth2 resource;
    private CompleteHandler completeHandler;

    /**
     * @constructor
     **/
    public InstagramApplicationCenter() {
        cachedAuthInfo = getCachedAuthInfo();
        accessToken = cachedAuthInfo != null ? cachedAuthInfo.getAccessToken() : null;
        code = cachedAuthInfo != null ? cachedAuthInfo.getCode() : null;

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

    public boolean hasSession() {
        return accessToken != null;
    }

    public void login(Context context, CompleteHandler completeHandler) {
        if (invalidateResource(completeHandler))
            return;

        if (code != null) {
            refresh(completeHandler);
        } else {
            this.completeHandler = completeHandler;

            EventBus.getDefault().register(this);

            Intent intent = new Intent(context, InstagramLoginActivity.class);
            intent.putExtra("resource", resource);
            context.startActivity(intent);
        }
    }

    private void refresh(CompleteHandler completeHandler) {
        if (!invalidateCode(completeHandler))
            requestAccessToken(completeHandler);
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

            code = ((InstagramLoginActivity.InstagramLoginComplete) event).getCode();

            if (code != null) {
                requestAccessToken(completeHandler);
            }
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void requestAccessToken(CompleteHandler completeHandler) {
        Log.i("requestAccessToken");

        if (invalidateCode(completeHandler))
            return;

        Log.i("requestAccessToken valid");

        InstagramApiProxy.Service service = (InstagramApiProxy.Service) InstagramApiProxy.getDefault().getCurrentService();
        Call<Object> call = service.accessToken(resource.getParameters(OAuth2.InstagramParametersType.AccessToken));
        
        InstagramApiProxy.getDefault().enqueueCall(call, new Callback<Object>() {
            @Override
            public void onResponse(Response<Object> response, Retrofit retrofit) {
                Log.i("onResponse", GSonUtil.toGSonString(response.body()));
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i("onFailure");
                t.printStackTrace();
            }
        });
    }

//    - (void)accessToken:(SuccessBlock)success error:(ErrorBlock)error {
//        if ([self invalidateCode:error])
//        return;
//
//        [[HTTPActionManager sharedInstance] doAction:@"token" param:[self.model paramWithType:InstagramParametersTypeAccessToken] body:nil headers:nil success:^(id result) {
//            if (result) {
//                _accessToken = result[kOAuthProeprtyAccessTokenKey];
//                self.cachedAuthModel = [CachedAuthModel modelWithCode:self.code accessToken:self.accessToken];
//
//                if (success)
//                    success(result);
//
//                [self dequeueAPICallCommand];
//            }
//        } error:^(NSError *err) {
//            if (error)
//                error(err);
//        }];
//    }

    private boolean invalidateCode(CompleteHandler completeHandler) {
        if (code != null)
            return false;

        completeHandler.onError(new InstagramSDKError(
                InstagramSDKError.Code.NeedAuthorizationError,
                InstagramSDKError.Message.NeedAuthorizationError));
        return true;
    }

    private boolean invalidateResource(CompleteHandler completeHandler) {
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

    private InstagramAuth getCachedAuthInfo() {
        String cachedUser = getSharedPreference().getString(CACHED_INSTAGRAM_AUTH_INFO, null);
        if (cachedUser != null)
            return (InstagramAuth) GSonUtil.objectFromGSonString(cachedUser, InstagramAuth.class);
        return null;
    }

    private void setCachedAuthInfo(InstagramAuth authInfo) {
        SharedPreferences.Editor editor = getSharedPreference().edit();

        if (authInfo != null)
            editor.putString(CACHED_INSTAGRAM_AUTH_INFO, GSonUtil.toGSonString(authInfo));
        else
            editor.remove(CACHED_INSTAGRAM_AUTH_INFO);

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
        private String path;
        private Map<String, Object> parameters;
        CompleteHandler completeHandler;

        public CallCommand(String path, Map<String, Object> parameters, CompleteHandler completeHandler) {
            this.path = path;
            this.parameters = parameters;
            this.completeHandler = completeHandler;
        }

        public void clear() {
            path = null;
            parameters = null;
            completeHandler = null;
        }
    }

    // ================================================================================================
    //  Interface: CompleteHandler
    // ================================================================================================

    public interface CompleteHandler {
        void onError(InstagramSDKError error);
        void onComplete(AbstractModel model);
    }
}
