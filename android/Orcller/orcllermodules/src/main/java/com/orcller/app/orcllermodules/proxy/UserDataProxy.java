package com.orcller.app.orcllermodules.proxy;

import com.orcller.app.orcllermodules.BuildConfig;
import com.orcller.app.orcllermodules.model.api.ApiUser;

import retrofit.Call;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by pisces on 11/5/15.
 */
public class UserDataProxy extends AbstractDataProxy {

    // ================================================================================================
    //  Overridden: AbstractDataProxy
    // ================================================================================================

    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return BuildConfig.API_BASE_URL + "/users/";
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void loadUser(String userId, Callback<ApiUser.Profile> callback) {
        enqueueCall(service().loadUser(userId), callback);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private Service service() {
        return (Service) getCurrentService();
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @GET("{user_id}")
        Call<ApiUser.Profile> loadUser(@Path("user_id") String userId);
    }
}
