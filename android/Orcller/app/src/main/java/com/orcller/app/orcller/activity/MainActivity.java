package com.orcller.app.orcller.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.event.AlbumFlipViewEvent;
import com.orcller.app.orcller.facade.ApplicationFacade;
import com.orcller.app.orcller.fragment.ActivityFragment;
import com.orcller.app.orcller.fragment.CoeditListFragment;
import com.orcller.app.orcller.fragment.FindFriendsFragment;
import com.orcller.app.orcller.fragment.MainTabFragment;
import com.orcller.app.orcller.fragment.ProfileFragment;
import com.orcller.app.orcller.fragment.TimelineFragment;
import com.orcller.app.orcller.manager.MediaUploadUnit;
import com.orcller.app.orcller.widget.TabIndicator;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSViewPager;

public class MainActivity extends PSActionBarActivity
        implements TabHost.OnTabChangeListener, View.OnTouchListener, ViewPager.OnPageChangeListener {
    public static final String SELECTED_INDEX_KEY = "selectedIndex";
    private static final int TAB_COUNT = 5;
    private Map<String, MainTabFragment> fragmentMap = new HashMap<>();
    private PagerAdapter pagerAdapter;
    private TabHost tabHost;
    private PSViewPager viewPager;
    private MainTabFragment activedFragment;

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
        viewPager = (PSViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        tabHost.setup();
        tabHost.setOnTabChangedListener(this);
        viewPager.setOffscreenPageLimit(TAB_COUNT);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageMargin(GraphicUtils.convertDpToPixel(15));
        viewPager.setPageMarginDrawable(R.color.theme_lightgray_primary);
        viewPager.addOnPageChangeListener(this);
        addTabs();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateBadges();
        SharedObject.get().loadNewsCount();

        int selectedIndex = getIntent().getIntExtra(SELECTED_INDEX_KEY, -1);
        if (selectedIndex > -1 && selectedIndex != viewPager.getCurrentItem()) {
            viewPager.setCurrentItem(selectedIndex);
            getIntent().removeExtra(SELECTED_INDEX_KEY);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        SoftKeyboardNotifier.getDefault().unregister(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(0);
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        ApplicationFacade.clear();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof SharedObject.Event &&
                SharedObject.Event.CHANGE_NEWS_COUNT.equals(((SharedObject.Event) event).getType())) {
            updateBadges();
        } else if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                tabHost.getTabWidget().setVisibility(View.GONE);
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                tabHost.getTabWidget().setVisibility(View.VISIBLE);
            }
        } else if (event instanceof AlbumFlipViewEvent) {
            AlbumFlipViewEvent casted = (AlbumFlipViewEvent) event;

            if (AlbumFlipViewEvent.CANCEL_PANNING.equals(casted.getType()) ||
                    AlbumFlipViewEvent.PAGE_INDEX_CHANGE.equals(casted.getType()))
                viewPager.setPagingEnabled(true);
            else if (AlbumFlipViewEvent.START_PANNING.equals(casted.getType()))
                viewPager.setPagingEnabled(false);
        }
    }

    /**
     * View.OnTouchListener
     */
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int position = Integer.valueOf((String) v.getTag());

            if (position == viewPager.getCurrentItem()) {
                MainTabFragment fragment = (MainTabFragment) pagerAdapter.getItem(position);
                fragment.scrollToTop();
                return true;
            }
        }
        return false;
    }

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
        if (activedFragment != null) {
            activedFragment.setActive(false);
            activedFragment = null;
        }

        int position = Integer.valueOf(tag);
        viewPager.setCurrentItem(position);

        activedFragment = (MainTabFragment) pagerAdapter.getItem(position);
        String title = activedFragment.getToolbarTitle();

        getSupportActionBar().setTitle(title);
        getToolbar().setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);

        if (activedFragment.isUseSoftKeyboard())
            SoftKeyboardNotifier.getDefault().register(this);
        else
            SoftKeyboardNotifier.getDefault().unregister(this);

        activedFragment.invalidateFragment();
        activedFragment.setActive(true);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void addTabs() {
        for (int i=0; i<TAB_COUNT; i++) {
            TabIndicator indicator = new TabIndicator(this);
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(String.valueOf(i))
                    .setIndicator(indicator)
                    .setContent(new TabFactory(this));
            indicator.setBackgroundResource(R.drawable.background_ripple_tabbar_main);
            indicator.setDrawableLeft(getTabIconRes(i));
            indicator.setDrawableBound(getTabIconBound(i));
            indicator.setOnTouchListener(this);
            indicator.setTag(String.valueOf(i));
            tabHost.addTab(tabSpec);
        }
    }

    private int getBadgeCount(int position) {
        if (position == 0)
            return SharedObject.get().getTimelineCount();
        if (position == 1)
            return SharedObject.get().getRecommendCount();
        if (position == 2)
            return SharedObject.get().getCoeditCount();
        if (position == 3)
            return SharedObject.get().getActivityCount();
        if (position == 4)
            return SharedObject.get().getOptionsCount();
        return 0;
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

    private void updateBadges() {
        int count = tabHost.getTabWidget().getTabCount();

        for (int i=0; i<count; i++) {
            int badgeCount = getBadgeCount(i);
            TabIndicator indicator = (TabIndicator) tabHost.getTabWidget().getChildAt(i);
            indicator.getBadge().setText(badgeCount > 0 ? String.valueOf(badgeCount) : null);
        }

        Application.setBadge(this, SharedObject.get().getTimelineCount() + SharedObject.get().getActivityCount());
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
                MainTabFragment fragment;

                if (fragmentMap.containsKey(key)) {
                    fragment = fragmentMap.get(key);
                } else {
                    fragment = (MainTabFragment) fragmentClass.newInstance();
                    fragment.setActionBar(getSupportActionBar());
                    fragmentMap.put(key, fragment);
                }

                return fragment;
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    Log.d("e", e);
                return null;
            }
        }
    }
}
