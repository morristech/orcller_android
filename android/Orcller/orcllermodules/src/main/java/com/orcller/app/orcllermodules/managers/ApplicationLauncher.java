package com.orcller.app.orcllermodules.managers;

import android.support.annotation.NonNull;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import retrofit.Retrofit;

/**
 * Created by pisces on 11/3/15.
 */
public class ApplicationLauncher {
    private static final String APP_ID_KEY = "Application-Id";
    private static final String APP_VERSION_KEY = "Application-Version";
    private static final String DEVICE_NAME_KEY = "Device-Name";
    private static final String DEVIE_MODEL_KEY = "Device-Model";
    private static final String SYSTEM_NAME_KEY = "System-Name";
    private static final String SYSTEM_VERSION_KEY = "System-Version";
    private volatile static ApplicationLauncher uniqueInstance;

    private boolean initialized;
    private Headers.Builder headers;
    private ApplicationResource resource;

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

    public void ApplicationLauncher() {
        headers = new Headers().newBuilder();
    }

    public void launch() throws Exception {
        if (resource == null || resource.identifier == null || resource.packageName == null)
            throw new NullPointerException();

        if (!this.initialized) {
            this.setHeaders();
        }
    }

    public boolean initialized() {
        return this.initialized;
    }

    public void syncHeaders(Request.Builder builder) {
        builder.headers(headers);
    }

    public ApplicationResource getResource() {
        return resource;
    }

    public void setResource(ApplicationResource resource) {
        this.resource = resource;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setHeaders() {
//        TODO: Impl here!
        this.headers.add(APP_ID_KEY, resource.getIdentifier());
        this.headers.add(APP_VERSION_KEY, null);
        this.headers.add(DEVICE_NAME_KEY, null);
        this.headers.add(DEVIE_MODEL_KEY, null);
        this.headers.add(SYSTEM_NAME_KEY, null);
        this.headers.add(SYSTEM_VERSION_KEY, null);
    }

    // ================================================================================================
    //
    //  Class: ApplicationResource
    //
    // ================================================================================================

    public class ApplicationResource {
        private static final String BASE_URL = "https://market.android.com/details?id=";

        private String identifier;
        private String packageName;

        public void  ApplicationResource(String identifier, String packageName) {
            this.setIdentifier(identifier);
            this.setPackageName(packageName);
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getInstallUrl() {
            if (this.packageName == null)
                return null;
            return BASE_URL + this.packageName;
        }

        public String getPackageName() {
            return this.packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
    }

    // ================================================================================================
    //
    //  Class: ApplicationLauncherInitialized
    //
    // ================================================================================================

    public class ApplicationLauncherInitialized {
        public ApplicationLauncherInitialized() {
        }
    }
}
