package com.orcller.app.orcller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.Const;
import com.orcller.app.orcller.service.ApplicationService;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.ApplicationResource;

import de.greenrobot.event.EventBus;
import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.instagram.sdk.error.InstagramSDKError;
import pisces.psfoundation.model.AbstractModel;
import pisces.psfoundation.utils.GSonUtil;
import pisces.psfoundation.utils.Log;

public class SplashActivity extends Activity {

    // ================================================================================================
    //  Overridden: Activity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        startService(new Intent(this, ApplicationService.class));
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    // ================================================================================================
    //  Event Handler
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof ApplicationLauncher.ApplicationInitialized) {
            startMainActivity();
        } else if (event instanceof ApplicationLauncher.ApplicationHasNewVersion) {
            Log.i("ApplicationHasNewVersion", GSonUtil.toGSonString(event));

//            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MyActivity.this);
//            alert_confirm.setMessage("프로그램을 종료 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // 'YES'
//                        }
//                    }).setNegativeButton("취소",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // 'No'
//                            return;
//                        }
//                    });
//            AlertDialog alert = alert_confirm.create();
//            alert.show();
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
//        Class activityClass = AuthenticationCenter.getDefault().hasSession() ?
//                MainActivity.class : MemberActivity.class;
//        startActivity(new Intent(this, activityClass));
//        finish();


        InstagramApplicationCenter.getDefault().login(this, new InstagramApplicationCenter.CompleteHandler() {
            @Override
            public void onError(InstagramSDKError error) {

            }

            @Override
            public void onComplete(AbstractModel model) {

            }
        });
    }
}
