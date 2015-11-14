package pisces.instagram.sdk.proxy;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.instagram.sdk.error.InstagramSDKError;
import pisces.instagram.sdk.model.ApiInstagram;
import pisces.instagram.sdk.model.ApiInstagramResult;
import pisces.instagram.sdk.model.OAuth2;
import pisces.psfoundation.proxy.AbstractRetrofitProxy;
import pisces.psfoundation.utils.GSonUtil;
import pisces.psfoundation.utils.Log;
import retrofit.Call;
import retrofit.Callback;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramApiProxy<T> extends AbstractRetrofitProxy {
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
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = null;

                if (InstagramApplicationCenter.getDefault().hasSession()) {
                    HttpUrl url = original
                            .httpUrl()
                            .newBuilder()
                            .addQueryParameter(
                                    OAuth2.ACCESS_TOKEN_KEY,
                                    InstagramApplicationCenter.getDefault().getAccessToken())
                            .build();
                    request = original.newBuilder()
                            .url(url)
                            .method(original.method(), original.body())
                            .build();
                } else {
                    request = original.newBuilder()
                            .method(original.method(), original.body())
                            .build();
                }

                return chain.proceed(request);
            }
        });
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

    public void enqueueCall(final Call<ApiInstagramResult> call, final Callback<ApiInstagramResult> calllback) {
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

    public Service service() {
        return (Service) getCurrentService();
    }

    public interface Service {
        @FormUrlEncoded
        @POST("oauth/access_token")
        Call<ApiInstagram.AccessTokenRes> accessToken(@FieldMap Map<String, String> parameters);

        @GET("oauth/authorize")
        Call<Object> authorize();

        @GET("v1/users/{userId}/followed-by")
        Call<ApiInstagram.UserListRes> followedBy(
                @Path("userId") String userId,
                @Query("count") int count,
                @Query("after") String after);

        @GET("v1/users/{userId}/follows")
        Call<ApiInstagram.UserListRes> follows(
                @Path("userId") String userId,
                @Query("count") int count,
                @Query("after") String after);

        @GET("v1/media/popular")
        Call<ApiInstagram.MediaListRes> popularMedia(
                @Query("count") int count,
                @Query("after") String after);

        @GET("v1/users/{userId}/media/recent")
        Call<ApiInstagram.MediaListRes> recentMedia(
                @Path("userId") String userId,
                @Query("count") int count,
                @Query("after") String after);

        @GET("v1/media/search")
        Call<ApiInstagram.MediaListRes> searchMedia(
                @Query("lat") float lat,
                @Query("lng") float lng,
                @Query("distance") int distance);

        @GET("v1/media/search")
        Call<ApiInstagram.UserListRes> searchUser(@Query("q") String name);

        @GET("v1/users/{userId}")
        Call<ApiInstagram.UserRes> user(@Path("userId") String userId);
    }

    // ================================================================================================
    //  Interface: CompleteHandler
    // ================================================================================================

    public interface CompleteHandler {
        void onError(InstagramSDKError error);
        void onComplete(ApiInstagramResult result);
    }
}
