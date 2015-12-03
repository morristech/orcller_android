package com.orcller.app.orcller.facade;

import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.FacebookSdk;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.AlbumCreateActivity;
import com.orcller.app.orcller.activity.AlbumEditActivity;
import com.orcller.app.orcller.activity.AlbumPageDefaultActivity;
import com.orcller.app.orcller.activity.AlbumPageDeleteActivity;
import com.orcller.app.orcller.activity.AlbumPageOrderActivity;
import com.orcller.app.orcller.activity.MainActivity;
import com.orcller.app.orcller.activity.MediaListActivity;
import com.orcller.app.orcller.activity.MemberActivity;
import com.orcller.app.orcller.activity.PageListActivity;
import com.orcller.app.orcller.activity.imagepicker.FBImagePickerActivity;
import com.orcller.app.orcller.activity.imagepicker.IGImagePickerActivity;
import com.orcller.app.orcller.activity.imagepicker.IGPopularMediaGridActivity;
import com.orcller.app.orcller.common.Const;
import com.orcller.app.orcller.model.album.ImageMedia;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.album.Page;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.AlbumGridView;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.ImageMediaScrollView;
import com.orcller.app.orcller.widget.ImageMediaView;
import com.orcller.app.orcller.widget.MediaScrollView;
import com.orcller.app.orcller.widget.MediaView;
import com.orcller.app.orcller.widget.PageScrollView;
import com.orcller.app.orcller.widget.PageView;
import com.orcller.app.orcller.widget.UserPictureView;
import com.orcller.app.orcller.widget.VideoMediaView;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.managers.DeviceManager;
import com.orcller.app.orcllermodules.managers.GooglePlayServiceManager;
import com.orcller.app.orcllermodules.model.ApplicationResource;
import com.orcller.app.orcllermodules.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psuikit.imagepicker.ImagePickerActivity;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/28/15.
 */
public class ApplicationFacade {
    private static interface Interceptor {
        void intercept(Intent intent);
    }

    private static final String TAG = "ApplicationFacade";
    private static ApplicationFacade uniqueInstance;
    private Context context;

    public ApplicationFacade() {
        context = Application.applicationContext();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static ApplicationFacade getDefault() {
        if(uniqueInstance == null) {
            synchronized(ApplicationFacade.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new ApplicationFacade();
                }
            }
        }
        return uniqueInstance;
    }

    public void onTokenRefresh() {
        if (GooglePlayServiceManager.getDefault().checkPlayServices(context)) {
            InstanceID instanceID = InstanceID.getInstance(context);
            String token = null;
            try {
                synchronized (TAG) {
                    String default_senderId = context.getString(R.string.gcm_defaultSenderId);
                    String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                    token = instanceID.getToken(default_senderId, scope, null);
                    DeviceManager.getDefault().registerDeviceToken(token);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        FacebookSdk.sdkInitialize(Application.applicationContext());
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

    // ================================================================================================
    //  Listener
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

    private void startMainActivity() {
        Class activityClass = AuthenticationCenter.getDefault().hasSession() ?
                MainActivity.class : MemberActivity.class;
        Intent intent = new Intent(Application.applicationContext(), activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        Application.applicationContext().startActivity(intent);
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
//        testMediaScrollView();
//        testMediaListActivity();
//        testImagePicker();
//        testFBImagePicker();
//        testIGImagePicker();
//        testIGPopularMediaGrid();
//        testUserPictureView();
//        testAlbumCreateActivity();
//        testAlbumGridView();
//        testAlbumPageOrderActivity();
//        testAlbumPageDefaultActivity();
//        testAlbumPageDeleteActivity();
//        testMemberActivity();
//        testAlbumEditActivity();
//        testPageScrollView();
        testPageListActivity();
//        testCommentInputView();
    }

    private void testActivity(Class activityClass, Interceptor interceptor) {
        Intent intent = new Intent(context, activityClass);

        if (interceptor != null)
            interceptor.intercept(intent);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        Application.applicationContext().startActivity(intent);
    }

    // ================================================================================================
    //  Test
    // ================================================================================================

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
        AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                int w = Application.getTopActivity().getWindow().getDecorView().getWidth();
                int h = Application.getTopActivity().getWindow().getDecorView().getHeight();
                ImageMedia media = (ImageMedia) response.body().entity.pages.getPageAtIndex(1).media;
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
                Media media = response.body().entity.pages.getPageAtIndex(3).media;
                MediaScrollView view = new MediaScrollView(Application.applicationContext());
                view.setModel(media);
                Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(w, h));
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testMediaListActivity() {
        AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                final ArrayList<Media> items = new ArrayList<Media>();

                for (Page page : response.body().entity.pages.data) {
                    items.add(page.media);
                }

                MediaListActivity.startActivity(items, 0);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testImagePicker() {
        testActivity(ImagePickerActivity.class, null);
    }

    private void testFBImagePicker() {
        testActivity(FBImagePickerActivity.class, null);
    }

    private void testIGImagePicker() {
        testActivity(IGImagePickerActivity.class, null);
    }

    private void testIGPopularMediaGrid() {
        testActivity(IGPopularMediaGridActivity.class, null);
    }

    private void testUserPictureView() {
        User user = AuthenticationCenter.getDefault().getUser();
        user.user_picture = "profiles/profile_c81e728d9d4c2f636f067f89cc14862c_1444935252.jpg";

        UserPictureView view = new UserPictureView(context);
        view.setModel(user);
        Application.getTopActivity().addContentView(view, new LinearLayout.LayoutParams(150, 150));
    }

    private void testAlbumCreateActivity() {
        AlbumCreateActivity.show();
    }

    private void testAlbumEditActivity() {
        AlbumEditActivity.show(4);
    }

    private void testAlbumGridView() {
        AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                int w = Application.getTopActivity().getWindow().getDecorView().getWidth();
                int h = Application.getTopActivity().getWindow().getDecorView().getHeight();
                AlbumGridView view = new AlbumGridView(Application.applicationContext());
                view.setModel(response.body().entity);
                Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(w, h));
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testAlbumPageOrderActivity() {
        AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                AlbumPageOrderActivity.show(response.body().entity);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testAlbumPageDefaultActivity() {
        AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                AlbumPageDefaultActivity.show(response.body().entity);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testAlbumPageDeleteActivity() {
        AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                AlbumPageDeleteActivity.show(response.body().entity);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testMemberActivity() {
        testActivity(MemberActivity.class, null);
    }

    private void testPageScrollView() {
        AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                int w = Application.getTopActivity().getWindow().getDecorView().getWidth();
                Page page = response.body().entity.pages.getPageAtIndex(3);
                PageScrollView view = new PageScrollView(Application.applicationContext());
                view.setModel(page);
                Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(w, w));
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testPageListActivity() {
        AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                PageListActivity.show(response.body().entity, 0);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void testCommentInputView() {
        CommentInputView view = new CommentInputView(Application.applicationContext());
        Application.getTopActivity().addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}