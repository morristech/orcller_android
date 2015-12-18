package com.orcller.app.orcllermodules.managers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.ApiApplication;
import com.orcller.app.orcllermodules.model.ApplicationResource;
import com.orcller.app.orcllermodules.proxy.ApplicationDataProxy;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/3/15.
 */
public class ApplicationLauncher {
    private static final String APP_PREFERENCE_KEY = "Application-Preferences";
    private static final String APP_ID_KEY = "Application-Id";
    private static final String APP_VERSION_KEY = "Application-Version";
    private static final String DEVICE_NAME_KEY = "Device-Name";
    private static final String DEVIE_MODEL_KEY = "Device-Model";
    private static final String SYSTEM_NAME_KEY = "System-Name";
    private static final String SYSTEM_VERSION_KEY = "System-Version";
    private static final String CACHED_CURRENT_APP_VERSION_KEY = "Current-Application-Version";
    private static final String CACHED_ORIGINE_APP_VERSION_KEY = "Origin-Application-Version";
    private volatile static ApplicationLauncher uniqueInstance;

    private boolean initialized;
    private Headers.Builder headers;
    private ApplicationResource resource;
    private String originAppVersion;
    private String currentVersion;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static ApplicationLauncher getDefault() {
        if(uniqueInstance == null) {
            synchronized(ApplicationLauncher.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new ApplicationLauncher();
                }
            }
        }
        return uniqueInstance;
    }

    public ApplicationLauncher() {
        SharedPreferences preferences = getSharedPreference();
        currentVersion = preferences.getString(CACHED_CURRENT_APP_VERSION_KEY, null);
        originAppVersion = preferences.getString(CACHED_ORIGINE_APP_VERSION_KEY, null);
        headers = new Headers.Builder();
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public SharedPreferences getSharedPreference() {
        return Application.applicationContext()
                .getSharedPreferences(APP_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public void launch() {
        if (resource == null || resource.getIdentifier() == null || initialized)
            return;

        setHeaders();
        removeCachesAfterVersionChecking();

        if (currentVersion != null) {
            initComplete();

            loadVersion(new Callback<ApiApplication.Version>() {
                @Override
                public void onResponse(retrofit.Response<ApiApplication.Version> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        if (response.body().isSuccess()) {
                            ApiApplication.Version.Entity entity = response.body().entity;
                            currentVersion = entity.version;

                            if (Application.isLowerAppVersion(currentVersion))
                                EventBus.getDefault().post(new ApplicationHasNewVersion(entity));

                            cacheAppVersion();
                        } else {
                            processErrorState(APIError.create(response.body()));
                        }
                    } else {
                        processErrorState(APIError.create(response.code(), response.message()));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    processErrorState(APIError.create(APIError.APIErrorCodeUnknown, t.getMessage()));
                }
            });
        } else {
            initialVersionChecking();
        }
    }

    public boolean initialized() {
        return initialized;
    }

    public Request.Builder syncHeaders(Request.Builder builder) {
        builder.headers(headers.build());
        return builder;
    }

    public void openPlayStore() {
        final String appPackageName = Application.applicationContext().getPackageName();
        try {
            Uri uri = Uri.parse("market://details?id=" + appPackageName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            Application.applicationContext().startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            Application.applicationContext().startActivity(intent);
        }
    }

    public ApplicationResource getResource() {
        return resource;
    }

    public ApplicationLauncher setResource(ApplicationResource resource) {
        this.resource = resource;
        return this;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void cacheAppVersion() {
        SharedPreferences.Editor editor = getSharedPreference().edit();
        editor.putString(CACHED_ORIGINE_APP_VERSION_KEY, Application.getPackageVersionName());

        if (currentVersion != null)
            editor.putString(CACHED_CURRENT_APP_VERSION_KEY, currentVersion);

        editor.commit();
    }

    private void initComplete() {
        initialized = true;

        AuthenticationCenter.getDefault().synchorinze();
        EventBus.getDefault().post(new ApplicationInitialized());
    }

    private void initialVersionChecking() {
        loadVersion(new Callback<ApiApplication.Version>() {
            @Override
            public void onResponse(retrofit.Response<ApiApplication.Version> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    if (response.body().isSuccess()) {
                        ApiApplication.Version.Entity entity = response.body().entity;
                        currentVersion = entity.version;

                        if (Application.isLowerAppVersion(currentVersion))
                            EventBus.getDefault().post(new ApplicationHasNewVersion(entity));

                        initComplete();
                        cacheAppVersion();
                    } else {
                        processErrorState(APIError.create(response.body()));
                    }
                } else {
                    processErrorState(APIError.create(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                processErrorState(APIError.create(APIError.APIErrorCodeUnknown, t.getMessage()));
            }
        });
    }

    private void loadVersion(Callback<ApiApplication.Version> callback) {
        ApplicationDataProxy proxy = new ApplicationDataProxy();
        ApplicationDataProxy.Service service = (ApplicationDataProxy.Service) proxy.getCurrentService();
        Call<ApiApplication.Version> call = service.loadVersion();
        proxy.enqueueCall(call, callback);
    }

    private void processErrorState(APIError error) {
        initComplete();
        EventBus.getDefault().post(new OnFailure(error));
    }

    private void removeCaches() {

    }

    private void removeCachesAfterVersionChecking() {
        if (originAppVersion != null && !Application.equalsAppVersion(originAppVersion))
            removeCaches();
    }

    private void setHeaders() {
        try {
            headers.add(APP_ID_KEY, resource.getIdentifier());
            headers.add(APP_VERSION_KEY, Application.getPackageVersionName());
            headers.add(DEVICE_NAME_KEY, Build.DEVICE);
            headers.add(DEVIE_MODEL_KEY, Build.MODEL);
            headers.add(SYSTEM_NAME_KEY, DeviceManager.SYSTEM_NAME);
            headers.add(SYSTEM_VERSION_KEY, Build.VERSION.RELEASE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================================================================================
    //  Events
    // ================================================================================================

    public class ApplicationInitialized {
        public ApplicationInitialized() {
        }
    }
    public class ApplicationHasNewVersion {
        private ApiApplication.Version.Entity entity;

        public ApplicationHasNewVersion(ApiApplication.Version.Entity entity) {
            this.entity = entity;
        }

        public ApiApplication.Version.Entity getEntity() {
            return entity;
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
