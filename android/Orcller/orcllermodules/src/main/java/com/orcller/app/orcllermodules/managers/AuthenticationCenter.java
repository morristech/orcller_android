package com.orcller.app.orcllermodules.managers;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.facebook.login.LoginManager;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.APIResult;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import com.orcller.app.orcllermodules.model.api.ApiUser;
import com.orcller.app.orcllermodules.model.facebook.FBUser;
import com.orcller.app.orcllermodules.proxy.MemberDataProxy;
import com.orcller.app.orcllermodules.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import com.orcller.app.orcllermodules.utils.GSonUtil;
import com.orcller.app.orcllermodules.utils.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String FB_PROFILE_PARAM = "first_name,gender,id,link,locale,name,picture.width(800).height(800),timezone,updated_time,verified";
    private final String CACHED_SESSION_ENTITY_KEY = "kCachedSessionEntity";
    private final String CACHED_USER_KEY = "kCachedUser";
    private final String SESSION_COOKIE_NAME = "orcller_session_token";
    private final String[] COOKIE_DOMAINS = {"www.orcller.com", "192.168.10.100"};
    private String testUserSessionToken;
    private ArrayList<HttpCookie> sessionCookies;
    private CookieManager cookieManager;
    private ApiMember.LoginRes.Entity cachedSessionEntity;
    private User cachedUser;
    private User user;

    // ================================================================================================
    //  Public
    // ================================================================================================

    /**
     * @constructor
     **/
    public AuthenticationCenter() {
        sessionCookies = new ArrayList<HttpCookie>();
        cookieManager = new CookieManager();
        loadCaches();
    }

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

    public void join(ApiMember.JoinWithEmailReq req, Api.CompleteHandler completeHandler) {
        MemberDataProxy.Service service = (MemberDataProxy.Service) MemberDataProxy.getDefault().getCurrentService();
        Call<ApiMember.LoginRes> call = service.joinByEmail(req.map());
        requestLogin(call, completeHandler);
    }

    public void join(ApiMember.JoinWithIdpReq req, Api.CompleteHandler completeHandler) {
        MemberDataProxy.Service service = (MemberDataProxy.Service) MemberDataProxy.getDefault().getCurrentService();
        Call<ApiMember.LoginRes> call = service.joinByIdp(req.map());
        requestLogin(call, completeHandler);
    }

    public void login(ApiMember.LoginReq req, final Api.CompleteHandler completeHandler) {
        MemberDataProxy.Service service = (MemberDataProxy.Service) MemberDataProxy.getDefault().getCurrentService();
        Call<ApiMember.LoginRes> call = service.login(req.map());
        requestLogin(call, completeHandler);
    }

    public void login(ApiMember.LoginWithIdpReq req, final Api.CompleteHandler completeHandler) {
        MemberDataProxy.Service service = (MemberDataProxy.Service) MemberDataProxy.getDefault().getCurrentService();
        Call<ApiMember.LoginRes> call = service.loginByIdp(req.map());
        requestLogin(call, completeHandler);
    }

    public void loginWithFacebook(Object target, final Api.CompleteHandler completeHandler) {
        final Api.CompleteHandler handler = new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                if (completeHandler != null)
                    completeHandler.onComplete(result, error);
            }
        };

        Bundle parameters = new Bundle();
        parameters.putString("fields", FB_PROFILE_PARAM);
        FBSDKRequestQueue.currentQueue().request(
                target,
                "me",
                parameters,
                null,
                new FBSDKRequest.CompleteHandler() {
                    @Override
                    public void onComplete(final JSONObject result, APIError error) {
                        if (error == null) {
                            try {
                                ApiMember.LoginWithIdpReq req = new ApiMember.LoginWithIdpReq();
                                req.idp_type = ApiMember.IDProviderType.Facebook.getValue();
                                req.idp_user_id = result.getString("id");

                                login(req, new Api.CompleteHandler() {
                                    @Override
                                    public void onComplete(Object rs, APIError err) {
                                        if (err == null) {
                                            handler.onComplete(rs, err);
                                        } else {
                                            try {
                                                Log.i("result.toString()", result.toString());
                                                handler.onComplete(GSonUtil.objectFromGSonString(result.toString(), FBUser.class), err);
                                            } catch (Exception e) {
                                                handler.onComplete(null, err);
                                            }
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                APIError err = APIError.newInstance(
                                        APIError.APIErrorCodeUnknown,
                                        e.getMessage());
                                handler.onComplete(null, err);
                            }
                        } else {
                            handler.onComplete(null, error);
                        }
                    }
                });
    }

    public void logout(final Api.CompleteHandler completeHandler) {
        MemberDataProxy.Service service = (MemberDataProxy.Service) MemberDataProxy.getDefault().getCurrentService();
        Call<APIResult> call = service.logout();

        final Api.CompleteHandler handler = new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                if (completeHandler != null)
                    completeHandler.onComplete(result, error);
            }
        };

        MemberDataProxy.getDefault().enqueueCall(call, new Callback<APIResult>() {
            @Override
            public void onResponse(Response<APIResult> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    if (response.body().isSuccess()) {
                        synchronized (this) {
                            clear();
                            LoginManager.getInstance().logOut();
                            // TODO: Add Instagram logout

                            handler.onComplete(response.body(), null);
                            EventBus.getDefault().post(new LogoutComplete());
                        }
                    } else {
                        handler.onComplete(null, APIError.newInstance(response.body()));
                    }
                } else {
                    handler.onComplete(null, APIError.newInstance(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                handler.onComplete(null, APIError.newInstance(APIError.APIErrorCodeUnknown, t.getMessage()));
            }
        });
    }

    public void sendCertificationEmail(String email, final Api.CompleteHandler completeHandler) {
        MemberDataProxy.Service service = (MemberDataProxy.Service) MemberDataProxy.getDefault().getCurrentService();
        Call<APIResult> call = service.sendCertificationEmail(email);

        MemberDataProxy.getDefault().enqueueCall(call, new Callback<APIResult>() {
            @Override
            public void onResponse(Response<APIResult> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    if (response.body().isSuccess()) {
                        completeHandler.onComplete(response.body(), null);
                    } else {
                        completeHandler.onComplete(null, APIError.newInstance(response.body()));
                    }
                } else {
                    completeHandler.onComplete(null, APIError.newInstance(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                completeHandler.onComplete(null, APIError.newInstance(APIError.APIErrorCodeUnknown, t.getMessage()));
            }
        });
    }

    public void synchorinze() {
        if (getCachedSessionToken() == null)
            return;

        user = cachedUser != null ? cachedUser : null;
        String token = getCurrentToken();

        if (!token.isEmpty()) {
            String encodedToken = URLEncoder.encode(token);
            for (String cookieDomain : COOKIE_DOMAINS) {
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

    public void updateDevice() {
        if (!hasSession())
            return;

        MemberDataProxy.Service service = (MemberDataProxy.Service) MemberDataProxy.getDefault().getCurrentService();
        Call<ApiMember.LoginRes> call = service.joinByEmail(new ApiMember.BaseReq().map());

        MemberDataProxy.getDefault().enqueueCall(call, new Callback<APIResult>() {
            @Override
            public void onResponse(Response<APIResult> response, Retrofit retrofit) {
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
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
        cachedSessionEntity = null;
        cachedUser = null;
        user = null;
    }

    private void loadCaches() {
        cachedSessionEntity = getCachedSessionEntity();
        cachedUser = getCachedUser();
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

    private void requestLogin(Call<ApiMember.LoginRes> call, final Api.CompleteHandler completeHandler) {
        final Api.CompleteHandler handler = new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                if (completeHandler != null)
                    completeHandler.onComplete(result, error);
            }
        };

        MemberDataProxy.getDefault().enqueueCall(call, new Callback<ApiMember.LoginRes>() {
            @Override
            public void onResponse(Response<ApiMember.LoginRes> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    if (response.body().isSuccess()) {
                        synchronized (this) {
                            setCachedSessionEntity(response.body().entity);
                            loadCaches();
                            synchorinze();
                            handler.onComplete(response.body(), null);
                            EventBus.getDefault().post(new LoginComplete());
                        }
                    } else {
                        handler.onComplete(null, APIError.newInstance(response.body()));
                    }
                } else {
                    handler.onComplete(null, APIError.newInstance(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                handler.onComplete(null, APIError.newInstance(APIError.APIErrorCodeUnknown, t.getMessage()));
            }
        });
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

    // ================================================================================================
    //  Events
    // ================================================================================================

    public static class LoginComplete {
        public LoginComplete() {
        }
    }

    public static class LogoutComplete {
        public LogoutComplete() {
        }
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
