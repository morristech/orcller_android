package com.orcller.app.orcllermodules.proxy;

import com.orcller.app.orcllermodules.BuildConfig;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.model.api.ApiMember;

import java.util.Map;

import retrofit.Call;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by pisces on 11/6/15.
 */
public class MemberDataProxy extends AbstractDataProxy {
    private volatile static MemberDataProxy uniqueInstance;

    // ================================================================================================
    //  Overridden: AbstractDataProxy
    // ================================================================================================

    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return BuildConfig.API_BASE_URL + "/member/";
    }

    public static MemberDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(MemberDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new MemberDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void changePassword(Map<String, String> parameters, Callback<ApiMember.LoginRes> callback) {
        enqueueCall(service().changePassword(parameters), callback);
    }

    public void login(Map<String, String> parameters, Callback<ApiMember.LoginRes> callback) {
        enqueueCall(service().login(parameters), callback);
    }

    public void logout(Callback<ApiResult> callback) {
        enqueueCall(service().logout(), callback);
    }

    public void sendCertificationEmail(String email, Callback<ApiResult> callback) {
        enqueueCall(service().sendCertificationEmail(email), callback);
    }

    public void syncByIdp(Map<String, String> parameters, Callback<ApiResult> callback) {
        enqueueCall(service().syncByIdp(parameters), callback);
    }

    public void updateDevice(Map<String, String> parameters, Callback<ApiResult> callback) {
        enqueueCall(service().updateDevice(parameters), callback);
    }

    public void updateUserOptions(Map<String, String> parameters, Callback<ApiResult> callback) {
        enqueueCall(service().updateUserOptions(parameters), callback);
    }

    public Service service() {
        return (Service) getCurrentService();
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @FormUrlEncoded @POST("change_password")
        Call<ApiMember.LoginRes> changePassword(@FieldMap Map<String, String> parameters);

        @FormUrlEncoded @POST("login")
        Call<ApiMember.LoginRes> login(@FieldMap Map<String, String> parameters);

        @FormUrlEncoded @POST("login_idp")
        Call<ApiMember.LoginRes> loginByIdp(@FieldMap Map<String, String> parameters);

        @GET("logout")
        Call<ApiResult> logout();

        @FormUrlEncoded @POST("join_email")
        Call<ApiMember.LoginRes> joinByEmail(@FieldMap Map<String, String> parameters);

        @FormUrlEncoded @POST("join_idp")
        Call<ApiMember.LoginRes> joinByIdp(@FieldMap Map<String, String> parameters);

        @FormUrlEncoded @POST("send_certification_email")
        Call<ApiResult> sendCertificationEmail(@Field("email")String email);

        @FormUrlEncoded @POST("sync_idp")
        Call<ApiResult> syncByIdp(@FieldMap Map<String, String> parameters);

        @FormUrlEncoded @POST("device")
        Call<ApiResult> updateDevice(@FieldMap Map<String, String> parameters);

        @FormUrlEncoded @POST("user_options/update")
        Call<ApiResult> updateUserOptions(@FieldMap Map<String, String> parameters);
    }
}
