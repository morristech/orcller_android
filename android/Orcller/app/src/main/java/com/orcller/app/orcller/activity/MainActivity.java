package com.orcller.app.orcller.activity;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TabHost;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.fragment.ActivityFragment;
import com.orcller.app.orcller.fragment.CoeditListFragment;
import com.orcller.app.orcller.fragment.FindFriendsFragment;
import com.orcller.app.orcller.fragment.ProfileFragment;
import com.orcller.app.orcller.fragment.TimelineFragment;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;

import java.util.HashMap;
import java.util.Map;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSFragment;
import pisces.psuikit.widget.PSButton;

public class MainActivity extends PSActionBarActivity
        implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    private static final int TAB_COUNT = 5;
    private Map<String, PSFragment> fragmentMap = new HashMap<>();
    private PagerAdapter pagerAdapter;
    private TabHost tabHost;
    private ViewPager viewPager;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(null);

        tabHost = (TabHost) findViewById(R.id.tabHost);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        tabHost.setup();
        tabHost.setOnTabChangedListener(this);
        viewPager.setPageMargin(GraphicUtils.convertDpToPixel(15));
        viewPager.setPageMarginDrawable(android.R.color.white);
        viewPager.addOnPageChangeListener(this);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        addTabs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        tabHost = null;
        viewPager = null;
    }

    // ================================================================================================
    //  Interface Implemetaion
    // ================================================================================================

    /**
     * ViewPager.OnPageChangeListener
     */
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        tabHost.setCurrentTab(position);
    }

    public void onPageScrollStateChanged(int state) {
    }

    /**
     * TabHost.OnTabChangeListener
     */
    public void onTabChanged(String tag) {
        int position = Integer.valueOf(tag);
        viewPager.setCurrentItem(position, false);
        getSupportActionBar().setTitle(getTitle(position));
        getToolbar().setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        ((PSFragment) pagerAdapter.getItem(position)).startFragment();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void addTabs() {
        for (int i = 0; i < TAB_COUNT; i++) {
            PSButton indicator = new PSButton(this);
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(String.valueOf(i))
                    .setIndicator(indicator)
                    .setContent(new TabFactory(this));
            indicator.setBackgroundResource(R.drawable.background_ripple_tabbar_main);
            indicator.setDrawableLeft(getTabIconRes(i));
            indicator.setDrawableBound(getTabIconBound(i));
            tabHost.addTab(tabSpec);
        }
    }

    private Rect getTabIconBound(int position) {
        if (position == 4)
            return new Rect(0, 0, GraphicUtils.convertDpToPixel(17), GraphicUtils.convertDpToPixel(22));
        return new Rect(0, 0, GraphicUtils.convertDpToPixel(21), GraphicUtils.convertDpToPixel(21));
    }

    private int getTabIconRes(int position) {
        if (position == 0)
            return R.drawable.icon_tabbar_home;
        if (position == 1)
            return R.drawable.icon_tabbar_search;
        if (position == 2)
            return R.drawable.icon_tabbar_coedit;
        if (position == 3)
            return R.drawable.icon_tabbar_activity;
        if (position == 4)
            return R.drawable.icon_tabbar_profile;
        return 0;
    }

    private CharSequence getTitle(int position) {
        if (position == 2)
            return getString(R.string.w_title_collaborations);
        if (position == 3)
            return getString(R.string.w_title_activity);
        if (position == 4)
            return AuthenticationCenter.getDefault().getUser().user_id;
        return null;
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
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return getFragment(position, TimelineFragment.class);
            if (position == 1)
                return getFragment(position, FindFriendsFragment.class);
            if (position == 2)
                return getFragment(position, CoeditListFragment.class);
            if (position == 3)
                return getFragment(position, ActivityFragment.class);
            if (position == 4)
                return getFragment(position, ProfileFragment.class);
            return null;
        }

        private Fragment getFragment(int position, Class fragmentClass) {
            try {
                String key = String.valueOf(position);
                PSFragment fragment;

                if (fragmentMap.containsKey(key)) {
                    fragment = fragmentMap.get(key);
                } else {
                    fragment = (PSFragment) fragmentClass.newInstance();
                    fragmentMap.put(key, fragment);
                }

                return fragment;
            } catch (Exception e) {
                Log.d("e", e);
                return null;
            }
        }
    }
}
