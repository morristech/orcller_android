package com.orcller.app.orcllermodules.proxy;

import com.orcller.app.orcllermodules.BuildConfig;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import pisces.psfoundation.proxy.AbstractRetrofitProxy;
import pisces.psfoundation.utils.Log;
import retrofit.Call;
import retrofit.Callback;

/**
 * Created by pisces on 11/4/15.
 */
abstract public class AbstractDataProxy<T> extends AbstractRetrofitProxy {

    // ================================================================================================
    //  Overridden: AbstractRetrofitProxy
    // ================================================================================================

    @Override
    protected void interceptRequest(OkHttpClient httpClient) {
        httpClient.setReadTimeout(BuildConfig.HTTP_TIMEOUT_INTERVAL, TimeUnit.SECONDS);
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = ApplicationLauncher.getDefault()
                        .syncHeaders(original.newBuilder())
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        });
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void enqueueCall(final Call<ApiResult> call, final Callback<ApiResult> calllback) {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    call.enqueue(calllback);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG)
                        Log.e(e.getMessage(), e);
                }
            }
        }, "Background");

        thread.start();
    }
}
