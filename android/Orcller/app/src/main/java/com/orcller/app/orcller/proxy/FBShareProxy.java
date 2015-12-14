package com.orcller.app.orcller.proxy;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.api.ApiMedia;
import com.orcller.app.orcller.utils.ImageGenerator;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.DataLoadValidator;
import pisces.psfoundation.utils.Log;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/8/15.
 */
public class FBShareProxy {
    private static FBShareProxy uniqueInstance;
    private DataLoadValidator dataLoadValidator = new DataLoadValidator();

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static FBShareProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(FBShareProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new FBShareProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void share(final Album album) {
        if (FBSDKRequestQueue.currentQueue().isValidAccessToken()) {
            upload(album);
        } else {
            AuthenticationCenter.getDefault().syncWithFacebook(this, new Api.CompleteHandler() {
                @Override
                public void onComplete(Object result, APIError error) {
                    if (error == null) {
                        upload(album);
                    } else {
                        AlertDialogUtils.retry(error.getMessage(), new Runnable() {
                            @Override
                            public void run() {
                                share(album);
                            }
                        });
                    }
                }
            });
        }
    }

    private void upload(final Album album) {
        if (dataLoadValidator.invalidDataLoading())
            return;

        final ProgressDialog progressDialog = ProgressDialog.show(
                Application.getTopActivity(), null, Application.applicationContext().getString(R.string.w_preparing));
        final Runnable error = new Runnable() {
            @Override
            public void run() {
                progressDialog.hide();
                dataLoadValidator.endDataLoading();
                AlertDialogUtils.retry(R.string.m_fail_share, new Runnable() {
                    @Override
                    public void run() {
                        upload(album);
                    }
                });
            }
        };

        ImageGenerator.generateShareImage(album, new ImageGenerator.CompleteHandler() {
            @Override
            public void onComplete(final Bitmap bitmap) {
                MediaManager.getDefault().uploadImageDirectly(bitmap, new Callback<ApiMedia.UploadInfoRes>() {
                    @Override
                    public void onResponse(Response<ApiMedia.UploadInfoRes> response, Retrofit retrofit) {
                        bitmap.recycle();

                        if (response.isSuccess() && response.body().isSuccess()) {
                            progressDialog.hide();
                            dataLoadValidator.endDataLoading();

                            String description = album.getViewName() + (TextUtils.isEmpty(album.desc) ? "" : "- " + album.desc);
                            ShareLinkContent content = new ShareLinkContent.Builder()
                                    .setImageUrl(Uri.parse(SharedObject.toFullMediaUrl(response.body().entity.filename)))
                                    .setContentDescription(description)
                                    .setContentUrl(Uri.parse(SharedObject.getShareContentUrl(album.encrypted_id)))
                                    .build();

                            ShareDialog.show(Application.getTopActivity(), content);
                        } else {
                            if (DEBUG)
                                e("Api Error", response.body());

                            error.run();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (BuildConfig.DEBUG)
                            Log.e("onFailure", t);

                        bitmap.recycle();
                        error.run();
                    }
                });
            }
        });
    }
}
