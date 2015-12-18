package com.orcller.app.orcller.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.model.api.ApiCoedit;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import retrofit.Call;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.http.DELETE;
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
        @FormUrlEncoded
        @POST("accept")
        Call<ApiCoedit.ContributorsRes> accept(@Field("contributorId") String contributorId);

        @POST("{albumId}/ask")
        Call<ApiCoedit.ContributorsRes> ask(@Path("albumId") long albumId);

        @GET("{albumId}/asks")
        Call<ApiCoedit.ContributorsRes> asks(@Path("albumId") long albumId);

        @GET("{albumId}/contributors")
        Call<ApiCoedit.ContributorsRes> contributors(@Path("albumId") long albumId);

        @FormUrlEncoded
        @POST("{albumId}/invite")
        Call<ApiCoedit.ContributorsRes> invite(@Path("albumId") long albumId, @Field("user_ids") String userIds);

        @GET("{albumId}/invites")
        Call<ApiCoedit.ContributorsRes> invites(@Path("albumId") long albumId);

        @GET("{albumId}/standby")
        Call<ApiCoedit.ContributorsRes> standby(@Path("albumId") long albumId);

        @DELETE("{albumId}/ask")
        Call<ApiCoedit.ContributorsRes> unask(@Path("albumId") long albumId);

        @DELETE("{albumId}/invite/{userId}")
        Call<ApiCoedit.ContributorsRes> uninvite(@Path("albumId") long albumId, @Path("userId") long userId);
    }
}
