package com.orcller.app.orcllermodules.proxy;

import android.support.annotation.Nullable;

import com.orcller.app.orcllermodules.BuildConfig;
import com.orcller.app.orcllermodules.model.ApiApplication;
import com.orcller.app.orcllermodules.model.ApiResult;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by pisces on 11/4/15.
 */
public class ApplicationDataProxy extends AbstractDataProxy {
    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return BuildConfig.API_BASE_URL+ "/app/";
    }

    public interface Service {
        @GET("version")
        Call<ApiApplication.Version> loadVersion();

        @FormUrlEncoded
        @POST("crash")
        Call<ApiResult> sendCrashReport(@Field("report") String report);
    }
}