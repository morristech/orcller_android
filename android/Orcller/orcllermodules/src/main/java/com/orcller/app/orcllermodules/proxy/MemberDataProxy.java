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

    public void changePassword(ApiMember.ChangePasswordReq req, Callback<ApiMember.LoginRes> callback) {
        enqueueCall(service().changePassword(req.toMap()), callback);
    }

    public void login(ApiMember.LoginReq req, Callback<ApiMember.LoginRes> callback) {
        enqueueCall(service().login(req.toMap()), callback);
    }

    public void logout(Callback<ApiResult> callback) {
        enqueueCall(service().logout(), callback);
    }

    public void sendCertificationEmail(String email, Callback<ApiResult> callback) {
        enqueueCall(service().sendCertificationEmail(email), callback);
    }

    public void syncByIdp(ApiMember.SyncWithIdpReq req, Callback<ApiResult> callback) {
        enqueueCall(service().syncByIdp(req.toMap()), callback);
    }

    public void updateDevice(ApiMember.BaseReq req, Callback<ApiResult> callback) {
        enqueueCall(service().updateDevice(req.toMap()), callback);
    }

    public void updateUserOptions(ApiMember.UpdateUserOptionsReq req, Callback<ApiResult> callback) {
        enqueueCall(service().updateUserOptions(req.toMap()), callback);
    }

    public Service service() {
        return (Service) getCurrentService();
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @FormUrlEncoded @POST("change_password")
        Call<ApiMember.LoginRes> changePassword(@FieldMap Map<String, Object> parameters);

        @FormUrlEncoded @POST("login")
        Call<ApiMember.LoginRes> login(@FieldMap Map<String, Object> parameters);

        @FormUrlEncoded @POST("login_idp")
        Call<ApiMember.LoginRes> loginByIdp(@FieldMap Map<String, Object> parameters);

        @GET("logout")
        Call<ApiResult> logout();

        @FormUrlEncoded @POST("join_email")
        Call<ApiMember.LoginRes> joinByEmail(@FieldMap Map<String, Object> parameters);

        @FormUrlEncoded @POST("join_idp")
        Call<ApiMember.LoginRes> joinByIdp(@FieldMap Map<String, Object> parameters);

        @FormUrlEncoded @POST("send_certification_email")
        Call<ApiResult> sendCertificationEmail(@Field("email")String email);

        @FormUrlEncoded @POST("sync_idp")
        Call<ApiResult> syncByIdp(@FieldMap Map<String, Object> parameters);

        @FormUrlEncoded @POST("device")
        Call<ApiResult> updateDevice(@FieldMap Map<String, Object> parameters);

        @FormUrlEncoded @POST("user_options/update")
        Call<ApiResult> updateUserOptions(@FieldMap Map<String, Object> parameters);
    }
}
