package com.orcller.app.orcller.proxy;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.api.ApiRelationships;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import retrofit.Call;
import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by pisces on 12/12/15.
 */
public class RelationshipsDataProxy extends AbstractDataProxy {
    private static RelationshipsDataProxy uniqueInstance;

    // ================================================================================================
    //  Overridden: AbstractDataProxy
    // ================================================================================================

    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return BuildConfig.API_BASE_URL + "/relationships/";
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static RelationshipsDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(RelationshipsDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new RelationshipsDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void follow(long userId, Callback<ApiRelationships.FollowRes> callback) {
        enqueueCall(service().follow(userId), callback);
    }

    public void unfollow(long userId, Callback<ApiRelationships.FollowRes> callback) {
        enqueueCall(service().unfollow(userId), callback);
    }

    public Service service() {
        return (Service) getCurrentService();
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @POST("{userId}/follow")
        Call<ApiRelationships.FollowRes> follow(@Path("userId") long userId);

        @GET("{userId}/followers")
        Call<ApiRelationships.UserListRes> followers(@Path("userId") long userId);

        @GET("{userId}/follows")
        Call<ApiRelationships.UserListRes> following(@Path("userId") long userId);

        @GET("recommends")
        Call<ApiRelationships.UserListRes> recommends();

        @DELETE("{userId}/follow")
        Call<ApiRelationships.FollowRes> unfollow(@Path("userId") long userId);
    }
}
