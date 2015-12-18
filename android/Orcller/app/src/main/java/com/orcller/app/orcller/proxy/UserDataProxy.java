package com.orcller.app.orcller.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.ApiUser;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by pisces on 12/11/15.
 */
public class UserDataProxy extends AbstractDataProxy {
    private static UserDataProxy uniqueInstance;

    // ================================================================================================
    //  Overridden: AbstractDataProxy
    // ================================================================================================

    @Override
    protected Converter.Factory createConverterFactory() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Media.class, new MediaDeserializer())
                .create();
        return GsonConverterFactory.create(gson);
    }

    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return BuildConfig.API_BASE_URL + "/users/";
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static UserDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(UserDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new UserDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void coediting(int limit, String after, Callback<ApiUsers.CoeditListRes> callback) {
        enqueueCall(service().coediting(AuthenticationCenter.getDefault().getUser().user_uid, limit, after), callback);
    }

    public void count(long userId, Callback<ApiUsers.CountRes> callback) {
        enqueueCall(service().count(userId), callback);
    }

    public void newUserPictureName(Callback<ApiUsers.NewUserPictureNameRes> callback) {
        enqueueCall(service().newUserPictureName(), callback);
    }

    public void profile(long userId, Callback<ApiUser.Profile> callback) {
        enqueueCall(service().profile(userId), callback);
    }

    public void saveProfile(User user, Callback<ApiResult> callback) {
        enqueueCall(service().saveProfile(user.user_uid, user.user_name, user.user_profile_message), callback);
    }

    public Service service() {
        return (Service) getCurrentService();
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @GET("{userId}/album")
        Call<ApiUsers.AlbumListRes> albums(@Path("userId") long userId);

        @GET("{userId}/coediting")
        Call<ApiUsers.CoeditListRes> coediting(
                @Path("userId") long userId, @Query("limit") int limit, @Query("after") String after);

        @GET("{userId}/count")
        Call<ApiUsers.CountRes> count(@Path("userId") long userId);

        @GET("{userId}/favorites")
        Call<ApiUsers.AlbumListRes> favorites(@Path("userId") long userId);

        @GET("{userId}/media")
        Call<ApiUsers.MediaListRes> media(@Path("userId") long userId);

        @POST("picture")
        Call<ApiUsers.NewUserPictureNameRes> newUserPictureName();

        @GET("{userId}")
        Call<ApiUser.Profile> profile(@Path("userId") long userId);

        @FormUrlEncoded
        @POST("{userId}")
        Call<ApiResult> saveProfile(
                @Path("userId") long userId,
                @Field("user_name") String userName,
                @Field("user_profile_message") String profile);
    }
}
