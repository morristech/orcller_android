package com.orcller.app.orcllermodules.proxy;

import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.model.APIResult;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/4/15.
 */
abstract public class AbstractDataProxy<T> {
    private String baseUrl;
    private Class<T> serviceClass;
    private Converter.Factory converterFactory;
    private Retrofit retrofit;
    private T service;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public AbstractDataProxy() {
        this.baseUrl = this.createBaseUrl();
        this.converterFactory = this.createConverterFactory();
        this.serviceClass = this.createServiceClass();
        this.setUp();
    }

    public void enqueueCall(final Call<APIResult> call, final Callback<APIResult> calllback) {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    call.enqueue(calllback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Background");

        thread.start();
    }

    public T getCurrentService() {
        return this.service;
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected Converter.Factory createConverterFactory() {
        return GsonConverterFactory.create();
    }

    abstract protected Class<T> createServiceClass();

    abstract protected String createBaseUrl();

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setUp() {
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                Request request = ApplicationLauncher.getDefault()
                      .syncHeaders(original.newBuilder())
                      .method(original.method(), original.body())
                      .build();
                return chain.proceed(request);
            }
        });

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(this.baseUrl)
                        .client(httpClient);

        if (this.converterFactory != null)
            builder.addConverterFactory(this.converterFactory);

        retrofit = builder.build();
        service = retrofit.create(this.serviceClass);
    }
}
