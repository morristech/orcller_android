package com.orcller.app.orcller.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.api.ApiNotification;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by pisces on 12/18/15.
 */
public class ActivityDataProxy extends AbstractDataProxy {
    private static ActivityDataProxy uniqueInstance;

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
        return BuildConfig.API_BASE_URL + "/";
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static ActivityDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(ActivityDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new ActivityDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void list(int limit, String after, Callback<ApiNotification.NotificationListRes> callback) {
        enqueueCall(service().list(limit, after), callback);
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
        @GET("notification")
        Call<ApiNotification.NotificationListRes> list(@Query("limit") int limit, @Query("after") String after);
    }
}