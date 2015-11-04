package com.orcller.app.orcllermodules.proxy;

import com.orcller.app.orcllermodules.model.APIApplicationVersion;
import com.orcller.app.orcllermodules.model.APIResult;
import retrofit.Call;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.http.GET;

/**
 * Created by pisces on 11/4/15.
 */
public class ApplicationDataProxy extends AbstractDataProxy {
    @Override
    protected Class<ApplicationService> createServiceClass() {
        return ApplicationService.class;
    }

    @Override
    protected Converter.Factory createConverterFactory() {
        return GsonConverterFactory.create();
    }

    @Override
    protected String createBaseUrl() {
        return "https://www.orcller.com/apis/app/";
    }

    public interface ApplicationService {
        @GET("version")
        Call<APIApplicationVersion> loadVersion();
    }
}