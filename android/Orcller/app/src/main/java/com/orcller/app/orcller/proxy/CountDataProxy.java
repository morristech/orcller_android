package com.orcller.app.orcller.proxy;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.api.ApiCount;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import retrofit.Call;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by pisces on 12/18/15.
 */
public class CountDataProxy extends AbstractDataProxy {
    private static CountDataProxy uniqueInstance;

    // ================================================================================================
    //  Overridden: AbstractDataProxy
    // ================================================================================================

    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return BuildConfig.API_BASE_URL + "/count/";
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static CountDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(CountDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new CountDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void news(Callback<ApiCount.NewsCountRes> callback) {
        enqueueCall(service().news(
                UserDataProxy.getDefault().getLastViewDate(),
                TimelineDataProxy.getDefault().getLastViewDate(),
                ActivityDataProxy.getDefault().getLastViewDate()), callback);
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
        @GET("news")
        Call<ApiCount.NewsCountRes> news(
                @Query("coediting_time") long coediting_time,
                @Query("newsfeed_time") long newsfeed_time,
                @Query("notification_time") long notification_time);
    }
}