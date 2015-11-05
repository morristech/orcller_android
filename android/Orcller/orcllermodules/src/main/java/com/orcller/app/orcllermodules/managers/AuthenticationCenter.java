package com.orcller.app.orcllermodules.managers;

import android.content.SharedPreferences;
import android.util.Log;

import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import com.orcller.app.orcllermodules.model.api.ApiUser;
import com.orcller.app.orcllermodules.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.utils.GSonUtil;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/5/15.
 */
public class AuthenticationCenter {
    private volatile static AuthenticationCenter uniqueInstance;

    private final String CACHED_SESSION_ENTITY_KEY = "kCachedSessionEntity";
    private final String CACHED_USER_KEY = "kCachedUser";
    private final String SESSION_COOKIE_NAME = "orcller_session_token";
    private final String[] cookieDomains = {"www.orcller.com", "192.168.10.100"};

    private String testUserSessionToken;
    private ArrayList<HttpCookie> sessionCookies;
    private CookieManager cookieManager;
    private ApiMember.LoginRes.Entity cachedSessionEntity;
    private User cachedUser;
    private User user;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public AuthenticationCenter() {
        sessionCookies = new ArrayList<HttpCookie>();
        cookieManager = new CookieManager();
        cachedSessionEntity = getCachedSessionEntity();
        cachedUser = getCachedUser();
    }

    /**
     *  @constructor
     **/
    public static AuthenticationCenter getDefault() {
        if(uniqueInstance == null) {
            synchronized(AuthenticationCenter.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new AuthenticationCenter();
                }
            }
        }
        return uniqueInstance;
    }

    public void join(ApiMember.JoinWithEmailReq req,
                     Callback<ApiMember.LoginRes> success,
                     Callback<ApiMember.LoginRes> failure) {
        // TODO: Impl JoinWithEmail
    }

    public void join(ApiMember.JoinWithIdpReq req,
                     Callback<ApiMember.LoginRes> success,
                     Callback<ApiMember.LoginRes> failure) {
        // TODO: Impl JoinWithIdp
    }

    public void logout() {

    }

    public void synchorinze() {
        if (getCachedSessionToken() == null)
            return;

        user = cachedUser != null ? cachedUser : null;
        String token = getCurrentToken();

        if (!token.isEmpty()) {
            String encodedToken = URLEncoder.encode(token);
            for (String cookieDomain : cookieDomains) {
                HttpCookie cookie = new HttpCookie(SESSION_COOKIE_NAME, encodedToken);
                cookie.setDomain(cookieDomain);
                cookie.setPath("/");
                sessionCookies.add(cookie);
                cookieManager.getCookieStore().add(URI.create(cookieDomain), cookie);
            }

            CookieHandler.setDefault(cookieManager);
            loadUser();
        }
    }

    public void synchorinzeUser(final User user) {
        final AuthenticationCenter self = this;
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                setCachedUser(user);

                if (self.user != null) {
                    self.user.synchronize(user);
                } else {
                    self.user = user;
                }
            }
        }, "Background");

        thread.start();
    }

    public String getCurrentToken() {
        return getCachedSessionToken();
    }

    public boolean hasSession() {
        return getCachedSessionToken() != null;
    }

    public User getUser() {
        return user;
    }

    public AuthenticationCenter setTestUserSessionToken(String token) {
        testUserSessionToken = token;
        return this;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void clear() {
        for (HttpCookie cookie : sessionCookies) {
            cookieManager.getCookieStore().remove(URI.create(cookie.getDomain()), cookie);
        }

        setCachedSessionEntity(null);
        setCachedUser(null);
        user = null;
    }

    private void loadUser() {
        UserDataProxy proxy = new UserDataProxy();
        UserDataProxy.Service service = (UserDataProxy.Service) proxy.getCurrentService();
        Call<ApiUser.Profile> call = service.loadUser("me");

        proxy.enqueueCall(call, new Callback<ApiUser.Profile>() {
            @Override
            public void onResponse(Response<ApiUser.Profile> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    if (response.body().isSuccess()) {
                        synchorinzeUser(response.body().entity);
                    } else {
                        processErrorState(APIError.newInstance(response.body()));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                processErrorState(APIError.newInstance(APIError.APIErrorCodeUnknown, t.getMessage()));
            }
        });
    }

    private void processErrorState(APIError error) {
        EventBus.getDefault().post(new OnFailure(error));
    }

    private String getCachedSessionToken() {
        if (testUserSessionToken != null)
            return testUserSessionToken;
        return cachedSessionEntity != null ? cachedSessionEntity.session_token : null;
    }

    private ApiMember.LoginRes.Entity getCachedSessionEntity() {
        String cachedSessionEntity = ApplicationLauncher.getDefault().getSharedPreference().getString(CACHED_SESSION_ENTITY_KEY, null);
        if (cachedSessionEntity != null)
            return (ApiMember.LoginRes.Entity) GSonUtil.objectFromGSonString(cachedSessionEntity, ApiMember.LoginRes.Entity.class);
        return null;
    }

    private void setCachedSessionEntity(ApiMember.LoginRes.Entity entity) {
        SharedPreferences.Editor editor = ApplicationLauncher.getDefault().getSharedPreference().edit();

        if (entity != null)
            editor.putString(CACHED_SESSION_ENTITY_KEY, GSonUtil.toGSonString(entity));
        else
            editor.remove(CACHED_SESSION_ENTITY_KEY);

        editor.commit();
    }

    private User getCachedUser() {
        String cachedUser = ApplicationLauncher.getDefault().getSharedPreference().getString(CACHED_USER_KEY, null);
        if (cachedUser != null)
            return (User) GSonUtil.objectFromGSonString(cachedUser, User.class);
        return null;
    }

    private void setCachedUser(User cachedUser) {
        SharedPreferences.Editor editor = ApplicationLauncher.getDefault().getSharedPreference().edit();

        if (cachedUser != null)
            editor.putString(CACHED_USER_KEY, GSonUtil.toGSonString(cachedUser));
        else
            editor.remove(CACHED_USER_KEY);

        editor.commit();
    }

    public class OnFailure {
        private APIError error;

        public OnFailure(APIError error) {
            this.error = error;
        }

        public APIError getError() {
            return error;
        }
    }
}
