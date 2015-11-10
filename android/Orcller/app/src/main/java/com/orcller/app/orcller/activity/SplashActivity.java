package com.orcller.app.orcller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.facebook.FacebookSdk;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.Const;
import com.orcller.app.orcller.service.RegistrationIntentService;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.managers.GooglePlayServiceManager;
import com.orcller.app.orcllermodules.model.ApplicationResource;
import com.orcller.app.orcllermodules.utils.GSonUtil;
import com.orcller.app.orcllermodules.utils.Log;

import de.greenrobot.event.EventBus;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        if (GooglePlayServiceManager.getDefault().checkPlayServices(this))
            startService(new Intent(this, RegistrationIntentService.class));

        FacebookSdk.sdkInitialize(getApplicationContext());
        EventBus.getDefault().register(this);

        if (ApplicationLauncher.getDefault().initialized()) {
            startCurrnetActivity();
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

    public void onEventMainThread(Object event) {
        if (event instanceof ApplicationLauncher.ApplicationInitialized) {
            Log.i("ApplicationInitialized");
            startCurrnetActivity();
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
//            ((ApplicationLauncher.OnFailure) event).getError().printStackTrace();
        } else if (event instanceof AuthenticationCenter.OnFailure) {
            // Invalid session token - logout
            Log.i("AuthenticationCenter.OnFailure", GSonUtil.toGSonString(((AuthenticationCenter.OnFailure) event).getError()));
        }
    }

    private void startCurrnetActivity() {
        if (AuthenticationCenter.getDefault().hasSession()) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SplashActivity.this, MemberActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
