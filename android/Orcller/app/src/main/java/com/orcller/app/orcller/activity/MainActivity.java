package com.orcller.app.orcller.activity;

import android.content.Context;
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
import com.orcller.app.orcller.fragment.FindFriendsFragment;
import com.orcller.app.orcller.fragment.ProfileFragment;
import com.orcller.app.orcller.fragment.TimelineFragment;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSFragment;
import pisces.psuikit.widget.PSButton;

public class MainActivity extends PSActionBarActivity
        implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    private static final int TAB_COUNT = 5;
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
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    // ================================================================================================
    //  Listener
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
        viewPager.setCurrentItem(Integer.valueOf(tag), true);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void addTabs() {
        for (int i=0; i<TAB_COUNT; i++) {
            PSButton indicator = new PSButton(this);
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(String.valueOf(i))
                    .setIndicator(indicator)
                    .setContent(new TabFactory(this));
            indicator.setBackgroundResource(R.drawable.background_ripple_tabbar_main);
            indicator.setDrawableLeft(getTabIconRes(i));
            tabHost.addTab(tabSpec);
        }
    }

    private int getTabIconRes(int position) {
//        if (position == 0)
//            return R.drawable.icon_profile_tab_album;
//        if (position == 1)
//            return R.drawable.icon_profile_tab_media;
        return 0;
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
                return new TimelineFragment();
            if (position == 1)
                return new FindFriendsFragment();
            if (position == 2)
                return new Fragment();
            if (position == 3)
                return new ActivityFragment();
            if (position == 4)
                return new ProfileFragment();
            return null;
        }
    }
}
