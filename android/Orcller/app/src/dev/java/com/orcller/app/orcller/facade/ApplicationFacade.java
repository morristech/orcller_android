package com.orcller.app.orcller.facade;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.facebook.FacebookSdk;
import com.orcller.app.orcller.AnalyticsTrackers;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.AlbumSlideShowActivity;
import com.orcller.app.orcller.activity.MainActivity;
import com.orcller.app.orcller.activity.MemberActivity;
import com.orcller.app.orcller.activity.UserPictureEditActivity;
import com.orcller.app.orcller.common.Const;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.model.Page;
import com.orcller.app.orcller.model.PushNotificationObject;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.OpenUrlProxy;
import com.orcller.app.orcller.service.GcmListenerService;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.managers.DeviceManager;
import com.orcller.app.orcllermodules.model.ApplicationResource;
import pisces.psuikit.utils.AlertDialogUtils;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.manager.ActivityManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/28/15.
 */
public class ApplicationFacade {
    private static ApplicationFacade uniqueInstance;
    private boolean initialized;
    private Context context;
    private Intent intent;
    private PushNotificationObject pushNotificationObject;

    public ApplicationFacade() {
        context = Application.getTopActivity();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void clear() {
        if (EventBus.getDefault().isRegistered(uniqueInstance))
            EventBus.getDefault().unregister(uniqueInstance);

        AnalyticsTrackers.clear();
        ActivityManager.clear();

        uniqueInstance = null;
    }

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

    public boolean isInitialized() {
        return initialized;
    }

    public void onTokenRefresh() {
        DeviceManager.getDefault().registerDeviceToken(context.getString(R.string.gcm_defaultSenderId), true);
    }

    public void run(Intent intent) {
        this.intent = intent;
        this.pushNotificationObject = (PushNotificationObject) intent.getSerializableExtra(GcmListenerService.PUSH_NOTIFICATION_OBJECT_KEY);

        if (initialized) {
            if (AuthenticationCenter.getDefault().hasSession() && ActivityManager.hasRunningActivity(MainActivity.class)) {
                Activity activity = ActivityManager.getRunningActivity(MainActivity.class);
                putPushNotificationExtra(activity.getIntent());
                Application.moveToBack(activity);
                SharedObject.get().loadNewsCountDireclty();
            }

            OpenUrlProxy.run(this.intent);
        } else {
            try {
                FacebookSdk.sdkInitialize(Application.applicationContext());
                DeviceManager.getDefault().registerDeviceToken(context.getString(R.string.gcm_defaultSenderId));
                MediaManager.getDefault().clearUnnecessaryItems();

                if (!EventBus.getDefault().isRegistered(this))
                    EventBus.getDefault().register(this);

                if (ApplicationLauncher.getDefault().initialized()) {
                    startMainActivity();
                } else {
                    AuthenticationCenter.getDefault()
                            .setTestUserSessionToken(BuildConfig.TEST_SESSION_TOKEN);
                    ApplicationLauncher.getDefault()
                            .setResource(new ApplicationResource(Const.APPLICATION_IDENTIFIER))
                            .launch();
                }

                initialized = true;
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    Log.d("ApplicationFacade Run", e);
            }
        }
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof ApplicationLauncher.ApplicationInitialized ||
                event instanceof AuthenticationCenter.LoginEvent) {
            startMainActivity();
        } else if (event instanceof ApplicationLauncher.ApplicationHasNewVersion) {
            ApplicationLauncher.ApplicationHasNewVersion casted = (ApplicationLauncher.ApplicationHasNewVersion) event;
            String desc = casted.getEntity().version_description;
            String message = context.getString(R.string.m_has_new_version) + (TextUtils.isEmpty(desc) ? "" : "\n\n" + desc);

            AlertDialogUtils.show(message, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        ApplicationLauncher.getDefault().openPlayStore();
                    }
                }
            }, context.getString(R.string.w_dismiss), context.getString(R.string.w_update));
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void putPushNotificationExtra(Intent intent) {
        try {
            Class aClass = Class.forName(intent.getComponent().getClassName());

            if (pushNotificationObject != null && MainActivity.class.equals(aClass)) {
                if (PushNotificationObject.Type.Album.equals(pushNotificationObject.type) ||
                        PushNotificationObject.Type.Relationships.equals(pushNotificationObject.type))
                    intent.putExtra(MainActivity.SELECTED_INDEX_KEY, 3);

                pushNotificationObject = null;
            }
        } catch (ClassNotFoundException e) {
        }
    }

    private void startMainActivity() {

        AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                AlbumSlideShowActivity.show(response.body().entity);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });


//        Class activityClass = AuthenticationCenter.getDefault().hasSession() ?
//                MainActivity.class : MemberActivity.class;
//        Intent intent = new Intent(Application.applicationContext(), activityClass);
//
//        putPushNotificationExtra(intent);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        Application.applicationContext().startActivity(intent);
//        OpenUrlProxy.run(this.intent);
    }
}