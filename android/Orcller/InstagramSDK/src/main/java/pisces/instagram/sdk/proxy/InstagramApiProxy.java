package pisces.instagram.sdk.proxy;

import com.squareup.okhttp.OkHttpClient;

import java.util.Map;

import pisces.psfoundation.proxy.AbstractRetrofitProxy;
import retrofit.Call;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramApiProxy extends AbstractRetrofitProxy {
    private static InstagramApiProxy uniqueInstance;

    // ================================================================================================
    //  Overridden: AbstractRetrofitProxy
    // ================================================================================================

    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return "https://api.instagram.com/";
    }

    @Override
    protected void interceptRequest(OkHttpClient httpClient) {
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static InstagramApiProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(InstagramApiProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new InstagramApiProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void enqueueCall(final Call<Object> call, final Callback<Object> calllback) {
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

    public interface Service {
        @POST("oauth/access_token")
        Call<Object> accessToken(Map<String, String> parameters);

        @GET("oauth/authorize")
        Call<Object> authorize();

        @GET("v1/media/popular")
        Call<Object> popularMedia();

        @GET("v1/media/search")
        Call<Object> searchMedia();

        @GET("v1/users/{userId}")
        Call<Object> user(@Path("userId") String userId);

        @GET("v1/users/{userId}/follows")
        Call<Object> follows(@Path("userId") String userId);

        @GET("v1/users/{userId}/media/recent")
        Call<Object> recentMedia(@Path("userId") String userId);
    }
}
