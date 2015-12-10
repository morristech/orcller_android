package com.orcller.app.orcller.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TabHost;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.fragment.ActivityFragment;
import com.orcller.app.orcller.fragment.FindFriendsFragment;
import com.orcller.app.orcller.fragment.TimelineFragment;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.ProfileHearderView;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.ApiUser;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/10/15.
 */
public class ProfileActivity extends PSActionBarActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    private static final String USER_UID_KEY = "user_uid";
    private long userUid;
    private User model;
    private PagerAdapter pagerAdapter;
    private TabHost tabHost;
    private ViewPager viewPager;
    private ProfileHearderView profileHearderView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        userUid = Long.valueOf(getIntent().getData().getQueryParameter(USER_UID_KEY));
        profileHearderView = (ProfileHearderView) findViewById(R.id.profileHearderView);
        tabHost = (TabHost) findViewById(R.id.tabHost);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        tabHost.setup();


        View view = new View(this);
        view.setBackgroundColor(Color.RED);

        TabHost.TabSpec tabSpec1 = tabHost.newTabSpec("1").setIndicator(view).setContent(new TabFactory(this));
        TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("2").setIndicator("2").setContent(new TabFactory(this));
        TabHost.TabSpec tabSpec3 = tabHost.newTabSpec("3").setIndicator("3").setContent(new TabFactory(this));

        tabHost.addTab(tabSpec1);
        tabHost.addTab(tabSpec2);
        tabHost.addTab(tabSpec3);
        tabHost.setOnTabChangedListener(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(this);

        load();
        SoftKeyboardNotifier.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SoftKeyboardNotifier.getDefault().unregister(this);
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide(this);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * TabHost.OnTabChangeListener
     */
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    public void onPageSelected(int position) {

    }

    public void onPageScrollStateChanged(int state) {

    }

    /**
     * ViewPager.OnPageChangeListener
     */
    public void onTabChanged(String tag) {

    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected User getModel() {
        return model;
    }

    protected void setModel(User model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        getSupportActionBar().setTitle(model.user_id);
        profileHearderView.setModel(model);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load() {
        if (userUid < 1 || invalidDataLoading())
            return;

        ProgressBarManager.show();

        UserDataProxy.getDefault().profile(userUid, new Callback<ApiUser.Profile>() {
            @Override
            public void onResponse(Response<ApiUser.Profile> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    setModel(response.body().entity);
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());

                    //TODO: imple exception view
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                endDataLoading();
            }
        });
    }

    // ================================================================================================
    //  Class: TabFactory
    // ================================================================================================

    private class TabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public TabFactory(Context context) {
            mContext = context;
        }

        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    // ================================================================================================
    //  Class: PagerAdapter
    // ================================================================================================

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new TimelineFragment();
                case 1:
                    return new FindFriendsFragment();
                case 2:
                    return new ActivityFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}
