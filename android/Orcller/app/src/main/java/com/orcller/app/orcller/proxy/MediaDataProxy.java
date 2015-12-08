package com.orcller.app.orcller.proxy;

import android.graphics.Bitmap;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.api.ApiMedia;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;

import pisces.psfoundation.ext.Application;
import retrofit.Call;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

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
        return BuildConfig.API_BASE_URL + "/media/";
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

    public void getUploadInfo(Callback<ApiMedia.UploadInfoRes> callback) {
        enqueueCall(service().uploadInfo(), callback);
    }

    public void uploadDirectly(Bitmap bitmap, Callback<ApiMedia.UploadInfoRes> callback) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        RequestBody body = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("Filedata", "image_media",
                        RequestBody.create(MediaType.parse("image/jpeg"), byteArrayOutputStream.toByteArray()))
                .build();
        enqueueCall(service().uploadDirectly(body), callback);
    }

    private Service service() {
        return (Service) getCurrentService();
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @POST("upload")
        Call<ApiMedia.UploadInfoRes> uploadDirectly(@Body RequestBody body);

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