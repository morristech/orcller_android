package com.orcller.app.orcller.proxy;

import com.orcller.app.orcller.model.api.ApiMedia;
import com.orcller.app.orcllermodules.model.APIResult;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import pisces.psfoundation.ext.Application;
import retrofit.Call;
import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by pisces on 12/3/15.
 */
public class MediaDataProxy extends AbstractDataProxy {
    private static MediaDataProxy uniqueInstance;

    // ================================================================================================
    //  Overridden: AbstractDataProxy
    // ================================================================================================

    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return Application.applicationContext().getString(com.orcller.app.orcllermodules.R.string.server_base_url) + "/media/";
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static MediaDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(AlbumDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new MediaDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void uploadDirectly(Callback<APIResult> callback) {

    }

    public void getUploadInfo(Callback<ApiMedia.UploadInfoRes> callback) {
        enqueueCall(service().uploadInfo(), callback);
    }

    private Service service() {
        return (Service) getCurrentService();
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @GET("upload_info")
        Call<ApiMedia.UploadInfoRes> uploadInfo();
    }

    // ================================================================================================
    //  Interface: CompleteHandler
    // ================================================================================================

    public interface CompleteHandler {
        void onComplete(boolean isSuccess);
    }
}