package com.orcller.app.orcllermodules.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.orcller.app.orcllermodules.ext.Application;
import com.orcller.app.orcllermodules.model.APIApplicationVersion;
import com.orcller.app.orcllermodules.model.ApplicationResource;
import com.orcller.app.orcllermodules.proxy.ApplicationDataProxy;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;

import de.greenrobot.event.EventBus;
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
    private static final String SYSTEM_NAME_VALUE = "Android OS";
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

    public static ApplicationLauncher getInstance() {
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
        SharedPreferences preferences = Application.applicationContext()
                .getSharedPreferences(APP_PREFERENCE_KEY, Context.MODE_PRIVATE);
        currentVersion = preferences.getString(CACHED_CURRENT_APP_VERSION_KEY, null);
        originAppVersion = preferences.getString(CACHED_ORIGINE_APP_VERSION_KEY, null);
        headers = new Headers.Builder();
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void launch() {
        if (resource == null || resource.getIdentifier() == null || initialized)
            return;

        setHeaders();
        removeCachesAfterVersionChecking();

        if (originAppVersion != null) {
            initComplete();

            getVersion(new Callback<APIApplicationVersion>() {
                @Override
                public void onResponse(retrofit.Response<APIApplicationVersion> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        APIApplicationVersion.Entity entity = response.body().entity;

                        if (entity != null) {
                            currentVersion = entity.version;

                            if (Application.isLower(currentVersion))
                                EventBus.getDefault().post(new ApplicationHasNewVersion(entity));

                            cacheAppVersion();
                        }
                    } else {
                        processErrorState(null);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    processErrorState(null);
                }
            });
        }else {
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
        SharedPreferences preferences = Application.applicationContext()
                .getSharedPreferences(APP_PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CACHED_ORIGINE_APP_VERSION_KEY, Application.getPackageVersionName());

        if (currentVersion != null)
            editor.putString(CACHED_CURRENT_APP_VERSION_KEY, currentVersion);

        editor.commit();
    }

    private void getVersion(Callback<APIApplicationVersion> callback) {
        ApplicationDataProxy proxy = new ApplicationDataProxy();
        ApplicationDataProxy.ApplicationService service = (ApplicationDataProxy.ApplicationService) proxy.getCurrentService();
        Call<APIApplicationVersion> call = service.loadVersion();
        proxy.enqueueCall(call, callback);
    }

    private void processErrorState(Error error) {
        initComplete();
    }

    private void initComplete() {
        initialized = true;

        EventBus.getDefault().post(new ApplicationInitialized());
    }

    private void initialVersionChecking() {
        getVersion(new Callback<APIApplicationVersion>() {
            @Override
            public void onResponse(retrofit.Response<APIApplicationVersion> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    APIApplicationVersion.Entity entity = response.body().entity;

                    if (entity != null) {
                        currentVersion = entity.version;

                        if (Application.isLower(currentVersion))
                            EventBus.getDefault().post(new ApplicationHasNewVersion(entity));

                        initComplete();
                        cacheAppVersion();
                    }
                } else {
                    processErrorState(null);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                processErrorState(null);
            }
        });
    }

    private void removeCaches() {

    }

    private void removeCachesAfterVersionChecking() {
        if (originAppVersion != null && !Application.isEquals(originAppVersion))
            removeCaches();
    }

    private void setHeaders() {
        try {
            headers.add(APP_ID_KEY, resource.getIdentifier());
            headers.add(APP_VERSION_KEY, Application.getPackageVersionName());
            headers.add(DEVICE_NAME_KEY, Build.MODEL);
            headers.add(DEVIE_MODEL_KEY, Build.DEVICE);
            headers.add(SYSTEM_NAME_KEY, SYSTEM_NAME_VALUE);
            headers.add(SYSTEM_VERSION_KEY, Build.VERSION.RELEASE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ApplicationInitialized {
        public ApplicationInitialized() {
        }
    }
    public class ApplicationHasNewVersion {
        private APIApplicationVersion.Entity entity;

        public ApplicationHasNewVersion(APIApplicationVersion.Entity entity) {
            this.entity = entity;
        }
    }
}
