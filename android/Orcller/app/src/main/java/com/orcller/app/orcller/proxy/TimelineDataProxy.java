package com.orcller.app.orcller.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by pisces on 12/8/15.
 */
public class TimelineDataProxy extends AbstractDataProxy {
    private static TimelineDataProxy uniqueInstance;

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
        return BuildConfig.API_BASE_URL + "/newsfeed/";
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static TimelineDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(TimelineDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new TimelineDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void hideAllAlbums(long userId, Callback<ApiResult> callback) {
        enqueueCall(service().hideAllAlbums(userId), callback);
    }

    public void hideAlbum(long albumId, Callback<ApiResult> callback) {
        enqueueCall(service().hideAlbum(albumId), callback);
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
        @FormUrlEncoded
        @POST("hidden/user/{userId}")
        Call<ApiResult> hideAllAlbums(@Path("userId") long userId);

        @FormUrlEncoded
        @POST("hidden/album/{albumId}")
        Call<ApiResult> hideAlbum(@Path("albumId") long albumId);
    }
}
