package pisces.psfoundation.proxy;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;

import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/13/15.
 */
abstract public class AbstractRetrofitProxy<T> {
    private String baseUrl;
    private Class<T> serviceClass;
    private Converter.Factory converterFactory;
    private Retrofit retrofit;
    protected T service;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public AbstractRetrofitProxy() {
        this.baseUrl = this.createBaseUrl();
        this.converterFactory = this.createConverterFactory();
        this.serviceClass = this.createServiceClass();
        this.setUp();
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

    abstract protected void interceptRequest(OkHttpClient httpClient);

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setUp() {
        OkHttpClient httpClient = new OkHttpClient();

        interceptRequest(httpClient);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(this.baseUrl)
                .client(httpClient);

        if (this.converterFactory != null)
            builder.addConverterFactory(this.converterFactory);

        retrofit = builder.build();
        service = retrofit.create(this.serviceClass);
    }
}
