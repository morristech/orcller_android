package com.orcller.app.orcller.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.facebook.FacebookSdk;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.MainActivity;
import com.orcller.app.orcller.activity.MemberActivity;
import com.orcller.app.orcller.common.Const;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.managers.DeviceManager;
import com.orcller.app.orcllermodules.managers.GooglePlayServiceManager;
import com.orcller.app.orcllermodules.model.ApplicationResource;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/11/15.
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
            startMainActivity();
        } else {
//        AuthenticationCenter.getDefault()
//                .setTestUserSessionToken("8mhO9Ra6lVENUYvLj50QdWVpcvzUYk+8nt2yec4b/7knfvNYhO61ziJ5hWykaJpfG2Xfm5DxQc37Uo1oVtUi0Vfi1HmBMJ8LQ864fHr83fP0WH00Hs7ifi2LNAG5a1GFZguPQBcVgHhRisvD/Z0XGQ==");
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
        Log.i("onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("onDestroy");
        EventBus.getDefault().unregister(this);
    }

    // ================================================================================================
    //  Event Handler
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof ApplicationLauncher.ApplicationInitialized ||
                event instanceof AuthenticationCenter.LoginComplete ||
                event instanceof AuthenticationCenter.LogoutComplete) {
            startMainActivity();
        } else if (event instanceof ApplicationLauncher.ApplicationHasNewVersion) {
            ApplicationLauncher.ApplicationHasNewVersion casted = (ApplicationLauncher.ApplicationHasNewVersion) event;
            String desc = casted.getEntity().version_description;
            String message = getResources().getString(R.string.m_has_new_version) + (TextUtils.isEmpty(desc) ? "" : "\n\n" + desc);

            AlertDialogUtils.show(message, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        ApplicationLauncher.getDefault().openPlayStore();
                    }
                }
            }, getResources().getString(R.string.w_dismiss), getResources().getString(R.string.w_update));
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
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        Application.applicationContext().startActivity(intent);
    }
}
