package com.orcller.app.orcllermodules.proxy;

import android.content.SharedPreferences;

import com.orcller.app.orcllermodules.BuildConfig;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import pisces.psfoundation.proxy.AbstractRetrofitProxy;
import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.Log;
import retrofit.Call;
import retrofit.Callback;

/**
 * Created by pisces on 11/4/15.
 */
abstract public class AbstractDataProxy<T> extends AbstractRetrofitProxy {
    private long lastViewDate;
    private String lastViewDateKey = getClass().getSimpleName();

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

    public long getLastViewDate() {
        if (lastViewDate < 1)
            lastViewDate = ApplicationLauncher.getDefault().getSharedPreference().getLong(getLastViewDateKey(), 0);
        return lastViewDate > 0 ? lastViewDate : DateUtil.toUnixtimestamp(new Date());
    }

    public void setLastViewDate(long lastViewDate) {
        if (lastViewDate == getLastViewDate())
            return;

        SharedPreferences.Editor editor = ApplicationLauncher.getDefault().getSharedPreference().edit();
        String key = getLastViewDateKey();

        if (lastViewDate > 0)
            editor.putLong(key, lastViewDate);
        else
            editor.remove(key);

        editor.commit();
    }

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

    // ================================================================================================
    //  Private
    // ================================================================================================

    private String getLastViewDateKey() {
        return lastViewDateKey + "-" + String.valueOf(AuthenticationCenter.getDefault().getUser().user_uid);
    }
}
