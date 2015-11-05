package com.orcller.app.orcller.activity;


import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.Const;
import com.orcller.app.orcller.fragment.ActivityFragment;
import com.orcller.app.orcller.fragment.FindFriendsFragment;
import com.orcller.app.orcller.fragment.ProfileFragment;
import com.orcller.app.orcller.fragment.TimelineFragment;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.ApplicationResource;
import com.orcller.app.orcllermodules.utils.GSonUtil;

import de.greenrobot.event.EventBus;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        AuthenticationCenter.getDefault()
                .setTestUserSessionToken("8mhO9Ra6lVENUYvLj50QdWVpcvzUYk+8nt2yec4b/7knfvNYhO61ziJ5hWykaJpfG2Xfm5DxQc37Uo1oVtUi0Vfi1HmBMJ8LQ864fHr83fP0WH00Hs7ifi2LNAG5a1GFZguPQBcVgHhRisvD/Z0XGQ==");
        ApplicationLauncher.getDefault()
                .setResource(new ApplicationResource(Const.APPLICATION_IDENTIFIER))
                .launch();

        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        mViewPager = null;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        Context mContext;

        public SectionsPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new TimelineFragment(mContext);
                case 1:
                    return new FindFriendsFragment(mContext);
                case 2:
                    return new ActivityFragment(mContext);
                case 3:
                    return new ProfileFragment(mContext);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    public void onEventMainThread(Object event) {
        if (event instanceof ApplicationLauncher.ApplicationInitialized) {
            Log.i("ApplicationInitialized", GSonUtil.toGSonString(event));

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
}
