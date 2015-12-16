package com.orcller.app.orcller.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

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
        return BuildConfig.API_BASE_URL + "/";
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

    public void list(int limit, String after, Callback<ApiUsers.AlbumListRes> callback) {
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
        @FormUrlEncoded
        @POST("newsfeed/hidden/user/{userId}")
        Call<ApiResult> hideAllAlbums(@Path("userId") long userId);

        @FormUrlEncoded
        @POST("newsfeed/hidden/album/{albumId}")
        Call<ApiResult> hideAlbum(@Path("albumId") long albumId);

        @GET("newsfeed")
        Call<ApiUsers.AlbumListRes> list(@Query("limit") int limit, @Query("after") String after);
    }
}
