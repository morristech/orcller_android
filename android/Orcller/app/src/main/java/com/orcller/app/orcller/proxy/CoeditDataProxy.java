package com.orcller.app.orcller.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.ApiUser;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by pisces on 12/11/15.
 */
public class CoeditDataProxy extends AbstractDataProxy {
    private static CoeditDataProxy uniqueInstance;

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
        return BuildConfig.API_BASE_URL + "/coediting/";
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static CoeditDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(CoeditDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new CoeditDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public Service service() {
        return (Service) getCurrentService();
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @GET("{userId}/album")
        Call<ApiUsers.AlbumListRes> albums(@Path("userId") long userId);
    }
}
