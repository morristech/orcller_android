package com.orcller.app.orcllermodules.proxy;

import com.orcller.app.orcllermodules.model.ApiApplication;

import retrofit.Call;
import retrofit.http.GET;

/**
 * Created by pisces on 11/4/15.
 */
public class ApplicationDataProxy extends AbstractDataProxy {
    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createSuffixUrl() {
        return "/app/";
    }

    public interface Service {
        @GET("version")
        Call<ApiApplication.Version> loadVersion();
    }
}