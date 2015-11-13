package com.orcller.app.orcllermodules.proxy;

import com.orcller.app.orcllermodules.R;
import com.orcller.app.orcllermodules.model.api.ApiUser;

import pisces.psfoundation.ext.Application;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by pisces on 11/5/15.
 */
public class UserDataProxy extends AbstractDataProxy {
    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return Application.applicationContext().getString(R.string.server_base_url) + "/users/";
    }

    public interface Service {
        @GET("{user_id}")
        Call<ApiUser.Profile> loadUser(@Path("user_id") String userId);
    }
}
