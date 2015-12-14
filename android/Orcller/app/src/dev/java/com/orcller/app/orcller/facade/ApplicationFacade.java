package com.orcller.app.orcller.facade;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

/**
 * Created by pisces on 11/28/15.
 */
public class ApplicationFacade {
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
            startMainActivity();
        } else {
//        AuthenticationCenter.getDefault()
//                .setTestUserSessionToken("8mhO9Ra6lVENUYvLj50QdWVpcvzUYk+8nt2yec4b/7knfvNYhO61ziJ5hWykaJpfG2Xfm5DxQc37Uo1oVtUi0Vfi1HmBMJ8LQ864fHr83fP0WH00Hs7ifi2LNAG5a1GFZguPQBcVgHhRisvD/Z0XGQ==");
            ApplicationLauncher.getDefault()
                    .setResource(new ApplicationResource(Const.APPLICATION_IDENTIFIER))
                    .launch();
        }
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof ApplicationLauncher.ApplicationInitialized ||
                event instanceof AuthenticationCenter.LoginComplete ||
                event instanceof AuthenticationCenter.LogoutComplete) {
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
}