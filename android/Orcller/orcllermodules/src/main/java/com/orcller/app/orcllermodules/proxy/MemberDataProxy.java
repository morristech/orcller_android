package com.orcller.app.orcllermodules.proxy;

import com.orcller.app.orcllermodules.model.APIResult;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import com.orcller.app.orcllermodules.model.api.ApiUser;

import java.util.Map;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;

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
    protected String createSuffixUrl() {
        return "/member/";
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
        @FormUrlEncoded
        @POST("login_idp")
        Call<ApiMember.LoginRes> loginByIdp(@FieldMap Map<String, String> parameters);

        @POST("join_email")
        Call<ApiMember.LoginRes> joinByEmail(String json);

        @FormUrlEncoded
        @POST("send_certification_email")
        Call<APIResult> sendCertificationEmail(@Field("email")String email);
    }
}
