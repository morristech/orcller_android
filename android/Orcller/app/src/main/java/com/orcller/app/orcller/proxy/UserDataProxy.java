package com.orcller.app.orcller.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcllermodules.model.api.ApiUser;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by pisces on 12/11/15.
 */
public class UserDataProxy extends AbstractDataProxy {
    private static UserDataProxy uniqueInstance;

    // ================================================================================================
    //  Overridden: AbstractDataProxy
    // ================================================================================================

    @Override
    protected Converter.Factory createConverterFactory() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Media.class, new MediaDeserializer())
                .create();
        return GsonConverterFactory.create(gson);
    }

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

    public static UserDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(UserDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new UserDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void profile(long userId, Callback<ApiUser.Profile> callback) {
        enqueueCall(service().profile(userId), callback);
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
        @GET("{userId}")
        Call<ApiUser.Profile> profile(@Path("userId") long userId);
    }
}
