package com.orcller.app.orcller.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.FacebookSdk;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.Const;
import com.orcller.app.orcller.model.album.ImageMedia;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.album.Page;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.ImageMediaScrollView;
import com.orcller.app.orcller.widget.ImageMediaView;
import com.orcller.app.orcller.widget.MediaScrollView;
import com.orcller.app.orcller.widget.MediaView;
import com.orcller.app.orcller.widget.PageView;
import com.orcller.app.orcller.widget.VideoMediaView;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.managers.DeviceManager;
import com.orcller.app.orcllermodules.managers.GooglePlayServiceManager;
import com.orcller.app.orcllermodules.model.ApplicationResource;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GSonUtil;
import pisces.psfoundation.utils.Log;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/15/15.
 */
public class ApplicationService extends Service {
    private static final String TAG = "ApplicationService";

    public ApplicationService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
        EventBus.getDefault().register(this);

        if (ApplicationLauncher.getDefault().initialized()) {
            runTestSuite();
        } else {
            AuthenticationCenter.getDefault()
                    .setTestUserSessionToken("8mhO9Ra6lVENUYvLj50QdWVpcvzUYk+8nt2yec4b/7knfvNYhO61ziJ5hWykaJpfG2Xfm5DxQc37Uo1oVtUi0Vfi1HmBMJ8LQ864fHr83fP0WH00Hs7ifi2LNAG5a1GFZguPQBcVgHhRisvD/Z0XGQ==");
            ApplicationLauncher.getDefault()
                    .setResource(new ApplicationResource(Const.APPLICATION_IDENTIFIER))
                    .launch();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (GooglePlayServiceManager.getDefault().checkPlayServices(this)) {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = null;
            try {
                synchronized (TAG) {
                    String default_senderId = getString(R.string.gcm_defaultSenderId);
                    String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                    token = instanceID.getToken(default_senderId, scope, null);
                    DeviceManager.getDefault().registerDeviceToken(token);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    // ================================================================================================
    //  Event Handler
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof ApplicationLauncher.ApplicationInitialized) {
            runTestSuite();
        } else if (event instanceof ApplicationLauncher.ApplicationHasNewVersion) {
        } else if (event instanceof ApplicationLauncher.OnFailure) {
            ((ApplicationLauncher.OnFailure) event).getError().printStackTrace();
        } else if (event instanceof AuthenticationCenter.OnFailure) {
            ((ApplicationLauncher.OnFailure) event).getError().printStackTrace();
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void runTestSuite() {
//        testImageMediaView();
//        testVideoMediaView();
//        testPageView();
//        testFlipView();
//        testAlbumFlipView();
//        testImageMediaScrollView();
        testMediaScrollView();
    }

    private void testImageMediaView() {
        AlbumDataProxy.getDefault().view(31, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                ImageMediaView view = new ImageMediaView(Application.applicationContext());
                view.setImageLoadType(MediaView.ImageLoadType.Thumbnail.getValue() | MediaView.ImageLoadType.StandardResoultion.getValue());
                view.setModel(response.body().entity.pages.getPageAtIndex(0).media);

                Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(480, 480));
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testVideoMediaView() {
        AlbumDataProxy.getDefault().view(56, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                VideoMediaView view = new VideoMediaView(Application.applicationContext());
                view.setImageLoadType(MediaView.ImageLoadType.Thumbnail.getValue() | MediaView.ImageLoadType.StandardResoultion.getValue());
                view.setModel(response.body().entity.pages.getPageAtIndex(4).media);

                Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(480, 480));
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testPageView() {
        AlbumDataProxy.getDefault().view(56, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                PageView view = new PageView(Application.applicationContext());
                view.setImageLoadType(MediaView.ImageLoadType.Thumbnail.getValue() | MediaView.ImageLoadType.StandardResoultion.getValue());
                view.setModel(response.body().entity.pages.getPageAtIndex(4));

                Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(480, 480));
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testFlipView() {
        AlbumDataProxy.getDefault().view(56, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                FlipView view = new FlipView(Application.applicationContext());
                List<Page> pages = Arrays.asList(response.body().entity.pages.getPageAtIndex(3), response.body().entity.pages.getPageAtIndex(4));

                Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(480, 480));
                view.setPages(pages);
                view.setX(480);

                view.doFlip(FlipView.Direction.Right);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testAlbumFlipView() {
        AlbumDataProxy.getDefault().view(56, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                int w = Application.getTopActivity().getWindow().getDecorView().getWidth();
                int pw = w/2;
                AlbumFlipView view = new AlbumFlipView(Application.applicationContext());
                view.setPageWidth(pw);
                view.setPageHeight(pw);
                view.setModel(response.body().entity);
                view.setPageIndex(0);

                Application.getTopActivity().addContentView(view, new LinearLayout.LayoutParams(w, pw));
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testImageMediaScrollView() {
        AlbumDataProxy.getDefault().view(31, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                int w = Application.getTopActivity().getWindow().getDecorView().getWidth();
                int h = Application.getTopActivity().getWindow().getDecorView().getHeight();
                ImageMedia media = (ImageMedia) response.body().entity.pages.getPageAtIndex(4).media;
                ImageMediaScrollView view = new ImageMediaScrollView(Application.applicationContext());
                view.setModel(media);
                Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(w, h));
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testMediaScrollView() {
        AlbumDataProxy.getDefault().view(56, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                int w = Application.getTopActivity().getWindow().getDecorView().getWidth();
                int h = Application.getTopActivity().getWindow().getDecorView().getHeight();
                Media media = response.body().entity.pages.getPageAtIndex(4).media;
                MediaScrollView view = new MediaScrollView(Application.applicationContext());
                view.setModel(media);
                Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(w, h));
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }
}