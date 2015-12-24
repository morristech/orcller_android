package com.orcller.app.orcllermodules.managers;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.facebook.login.LoginManager;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import com.orcller.app.orcllermodules.model.api.ApiUser;
import com.orcller.app.orcllermodules.model.facebook.FBUser;
import com.orcller.app.orcllermodules.proxy.MemberDataProxy;
import com.orcller.app.orcllermodules.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.psfoundation.event.Event;
import pisces.psfoundation.utils.GsonUtil;
import pisces.psfoundation.utils.Log;
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
        sessionCookies = new ArrayList<>();
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

    public void changePassword(ApiMember.ChangePasswordReq req, Api.CompleteHandler completeHandler) {
        final Api.CompleteHandler handler = newHandler(completeHandler);

        MemberDataProxy.getDefault().changePassword(req.map(), new Callback<ApiMember.LoginRes>() {
            @Override
            public void onResponse(Response<ApiMember.LoginRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    synchronize(response.body().entity);
                    handler.onComplete(response.body(), null);
                } else {
                    handler.onComplete(null, APIError.create(response.body()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                handler.onComplete(null, APIError.create(APIError.APIErrorCodeUnknown, t.getMessage()));
            }
        });
    }

    public void join(ApiMember.JoinWithEmailReq req, Api.CompleteHandler completeHandler) {
        requestLogin(MemberDataProxy.getDefault().service().joinByEmail(req.map()), completeHandler);
    }

    public void join(ApiMember.JoinWithIdpReq req, Api.CompleteHandler completeHandler) {
        requestLogin(MemberDataProxy.getDefault().service().joinByIdp(req.map()), completeHandler);
    }

    public void login(ApiMember.LoginReq req, final Api.CompleteHandler completeHandler) {
        requestLogin(MemberDataProxy.getDefault().service().login(req.map()), completeHandler);
    }

    public void login(ApiMember.LoginWithIdpReq req, final Api.CompleteHandler completeHandler) {
        requestLogin(MemberDataProxy.getDefault().service().loginByIdp(req.map()), completeHandler);
    }

    public void loginWithFacebook(
            Object target,
            final FBSDKRequest.CompleteHandler fbCompleteHandler,
            final Api.CompleteHandler completeHandler) {
        final Api.CompleteHandler handler = newHandler(completeHandler);
        Bundle parameters = new Bundle();
        parameters.putString("fields", FB_PROFILE_PARAM);
        FBSDKRequestQueue.currentQueue().request(
                target,
                "me",
                parameters,
                FBUser.class,
                new FBSDKRequest.CompleteHandler<FBUser>() {
                    @Override
                    public void onComplete(final FBUser result, APIError error) {
                        if (fbCompleteHandler != null)
                            fbCompleteHandler.onComplete(result, error);

                        if (error == null) {
                            ApiMember.LoginWithIdpReq req = new ApiMember.LoginWithIdpReq();
                            req.idp_type = ApiMember.IDProviderType.Facebook.value();
                            req.idp_user_id = result.id;

                            login(req, new Api.CompleteHandler() {
                                @Override
                                public void onComplete(Object rs, APIError err) {
                                    if (err == null) {
                                        handler.onComplete(rs, err);
                                    } else {
                                        try {
                                            handler.onComplete(result, err);
                                        } catch (Exception e) {
                                            handler.onComplete(null, err);
                                        }
                                    }
                                }
                            });
                        } else {
                            handler.onComplete(null, error);
                        }
                    }
                });
    }

    public void logout(final Api.CompleteHandler completeHandler) {
        final Api.CompleteHandler handler = newHandler(completeHandler);
        final Object target = this;

        MemberDataProxy.getDefault().logout(new Callback<ApiResult>() {
            @Override
            public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    synchronized (this) {
                        clear();
                        LoginManager.getInstance().logOut();
                        InstagramApplicationCenter.getDefault().logout();
                        handler.onComplete(response.body(), null);
                        EventBus.getDefault().post(new LoginEvent(LoginEvent.LOGOUT, target));
                    }
                } else {
                    handler.onComplete(null, APIError.create(response.body()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                handler.onComplete(null, APIError.create(APIError.APIErrorCodeUnknown, t.getMessage()));
            }
        });
    }

    public void sendCertificationEmail(String email, final Api.CompleteHandler completeHandler) {
        MemberDataProxy.getDefault().sendCertificationEmail(email, new Callback<ApiResult>() {
            @Override
            public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    completeHandler.onComplete(response.body(), null);
                } else {
                    completeHandler.onComplete(null, APIError.create(response.body()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                completeHandler.onComplete(null, APIError.create(APIError.APIErrorCodeUnknown, t.getMessage()));
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
        synchorinzeUser(user, null);
    }

    public void synchorinzeUser(final User user, final Runnable completeHandler) {
        final AuthenticationCenter self = this;

        setCachedUser(user);

        if (this.user != null) {
            this.user.synchronize(user, new Runnable() {
                @Override
                public void run() {
                    if (completeHandler != null)
                        completeHandler.run();
                }
            }, true);
        } else {
            this.user = user;

            if (completeHandler != null)
                completeHandler.run();
        }
    }

    public void syncWithFacebook(Object target, final Api.CompleteHandler completeHandler) {
        final Api.CompleteHandler handler = newHandler(completeHandler);
        Bundle parameters = new Bundle();
        parameters.putString("fields", FB_PROFILE_PARAM);
        FBSDKRequestQueue.currentQueue().request(
                target,
                "me",
                parameters,
                FBUser.class,
                new FBSDKRequest.CompleteHandler<FBUser>() {
                    @Override
                    public void onComplete(final FBUser result, APIError error) {
                        if (error == null) {
                            ApiMember.SyncWithIdpReq req = new ApiMember.SyncWithIdpReq();
                            req.idp_type = ApiMember.IDProviderType.Facebook.value();
                            req.idp_user_id = result.id;

                            syncWithIdp(req, new Api.CompleteHandler() {
                                @Override
                                public void onComplete(Object rs, APIError err) {
                                    if (err == null) {
                                        handler.onComplete(result, err);
                                    } else {
                                        LoginManager.getInstance().logOut();
                                        handler.onComplete(null, err);
                                    }
                                }
                            });
                        } else {
                            LoginManager.getInstance().logOut();
                            handler.onComplete(null, error);
                        }
                    }
                });
    }

    public void syncWithIdp(ApiMember.SyncWithIdpReq req, final Api.CompleteHandler completeHandler) {
        final Api.CompleteHandler handler = newHandler(completeHandler);

        MemberDataProxy.getDefault().syncByIdp(req.map(), new Callback<ApiResult>() {
            @Override
            public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    handler.onComplete(response.body(), null);
                } else {
                    handler.onComplete(null, APIError.create(response.body()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                handler.onComplete(null, APIError.create(APIError.APIErrorCodeUnknown, t.getMessage()));
            }
        });
    }

    public void updateDevice() {
        if (!hasSession())
            return;

        MemberDataProxy.getDefault().updateDevice(new ApiMember.BaseReq().map(), new Callback<ApiResult>() {
            @Override
            public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    public void updateUserOptions(final ApiMember.UpdateUserOptionsReq req, final Api.CompleteHandler completeHandler) {
        final Api.CompleteHandler handler = newHandler(completeHandler);

        MemberDataProxy.getDefault().updateUserOptions(req.map(), new Callback<ApiResult>() {
            @Override
            public void onResponse(final Response<ApiResult> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    try {
                        User clonedUser = (User) getUser().clone();
                        clonedUser.user_options.album_permission = req.user_options_album_permission;
                        clonedUser.user_options.pns_types = req.user_options_pns_types;

                        synchorinzeUser(clonedUser, new Runnable() {
                            @Override
                            public void run() {
                                handler.onComplete(response.body(), null);
                            }
                        });
                    } catch (CloneNotSupportedException e) {
                        handler.onComplete(null, APIError.create(APIError.APIErrorCodeUnknown, e.getMessage()));
                    }
                } else {
                    Log.d("response.body()", response.body(), response);
                    handler.onComplete(null, APIError.create(response.body()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                handler.onComplete(null, APIError.create(APIError.APIErrorCodeUnknown, t.getMessage()));
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
        new UserDataProxy().loadUser("me", new Callback<ApiUser.Profile>() {
            @Override
            public void onResponse(Response<ApiUser.Profile> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    if (response.body().isSuccess()) {
                        synchorinzeUser(response.body().entity);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private Api.CompleteHandler newHandler(final Api.CompleteHandler completeHandler) {
        return new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                if (completeHandler != null)
                    completeHandler.onComplete(result, error);
            }
        };
    }

    private void requestLogin(Call<ApiMember.LoginRes> call, final Api.CompleteHandler completeHandler) {
        final Api.CompleteHandler handler = newHandler(completeHandler);
        final Object target = this;

        MemberDataProxy.getDefault().enqueueCall(call, new Callback<ApiMember.LoginRes>() {
            @Override
            public void onResponse(Response<ApiMember.LoginRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    synchronize(response.body().entity);
                    handler.onComplete(response.body(), null);
                    EventBus.getDefault().post(new LoginEvent(LoginEvent.LOGIN, target));
                } else {
                    handler.onComplete(null, APIError.create(response.body()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                handler.onComplete(null, APIError.create(APIError.APIErrorCodeUnknown, t.getMessage()));
            }
        });
    }

    private void synchronize(ApiMember.LoginRes.Entity entity) {
        if (entity != null) {
            setCachedSessionEntity(entity);
            loadCaches();
            synchorinze();
        }
    }

    private String getCachedSessionToken() {
        if (testUserSessionToken != null)
            return testUserSessionToken;
        return cachedSessionEntity != null ? cachedSessionEntity.session_token : null;
    }

    private ApiMember.LoginRes.Entity getCachedSessionEntity() {
        String cachedSessionEntity = ApplicationLauncher.getDefault().getSharedPreference().getString(CACHED_SESSION_ENTITY_KEY, null);
        if (cachedSessionEntity != null)
            return GsonUtil.fromJson(cachedSessionEntity, ApiMember.LoginRes.Entity.class);
        return null;
    }

    private void setCachedSessionEntity(ApiMember.LoginRes.Entity entity) {
        SharedPreferences.Editor editor = ApplicationLauncher.getDefault().getSharedPreference().edit();

        if (entity != null)
            editor.putString(CACHED_SESSION_ENTITY_KEY, GsonUtil.toGsonString(entity));
        else
            editor.remove(CACHED_SESSION_ENTITY_KEY);

        editor.commit();
    }

    private User getCachedUser() {
        String cachedUser = ApplicationLauncher.getDefault().getSharedPreference().getString(CACHED_USER_KEY, null);
        if (cachedUser != null)
            return GsonUtil.fromJson(cachedUser, User.class);
        return null;
    }

    private void setCachedUser(User cachedUser) {
        SharedPreferences.Editor editor = ApplicationLauncher.getDefault().getSharedPreference().edit();

        if (cachedUser != null)
            editor.putString(CACHED_USER_KEY, GsonUtil.toGsonString(cachedUser));
        else
            editor.remove(CACHED_USER_KEY);

        editor.commit();
    }

    // ================================================================================================
    //  Events
    // ================================================================================================

    public static class LoginEvent extends Event {
        public static final String LOGIN = "login";
        public static final String LOGOUT = "logout";

        public LoginEvent(String type, Object target) {
            super(type, target);
        }
    }
}
