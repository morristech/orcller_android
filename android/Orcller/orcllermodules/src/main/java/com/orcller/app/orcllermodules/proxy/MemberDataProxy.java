package com.orcller.app.orcllermodules.proxy;

import com.orcller.app.orcllermodules.R;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.model.api.ApiMember;

import java.util.Map;

import pisces.psfoundation.ext.Application;
import retrofit.Call;
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

    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return Application.applicationContext().getString(R.string.server_base_url) + "/member/";
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

    public interface Service {
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
        Call<ApiMember> updateDevice(@FieldMap Map<String, String> parameters);
    }
}
