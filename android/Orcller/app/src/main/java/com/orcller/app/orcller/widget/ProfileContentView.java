package com.orcller.app.orcller.widget;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TabHost;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.fragment.UserDataGridFragment;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.model.User;

import java.util.List;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSTabHost;
import pisces.psuikit.widget.PSButton;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/11/15.
 */
public class ProfileContentView extends PSTabHost
        implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    private long userId;
    private DataSource dataSource;
    private PagerAdapter pagerAdapter;
    private TabHost tabHost;
    private ViewPager viewPager;

    public ProfileContentView(Context context) {
        super(context);
    }

    public ProfileContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProfileContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSTabHost
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_profile_content, this);

        tabHost = (TabHost) findViewById(R.id.tabHost);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        tabHost.setup();
        tabHost.setOnTabChangedListener(this);
        viewPager.setPageMargin(GraphicUtils.convertDpToPixel(15));
        viewPager.setPageMarginDrawable(android.R.color.white);
        viewPager.addOnPageChangeListener(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;

        if (pagerAdapter == null) {
            pagerAdapter = new PagerAdapter(dataSource.getGridFragmentManager());
            viewPager.setAdapter(pagerAdapter);
        }

        addTabs();
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;

        addTabs();
        loadCount();
    }

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
        if (dataSource == null || userId < 1 || tabHost.getTabWidget().getTabCount() > 0)
            return;

        for (int i=0; i<dataSource.getTabCount(); i++) {
            PSButton indicator = new PSButton(getContext());
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(String.valueOf(i))
                    .setIndicator(indicator)
                    .setContent(new TabFactory(getContext()));
            indicator.setBackgroundResource(R.drawable.background_tab_profile_content);
            indicator.setTextColor(getResources().getColorStateList(R.drawable.color_tab_profile_content));
            indicator.setTextSize(GraphicUtils.convertDpToPixel(13));
            indicator.setDrawableLeft(getTabIconRes(i));
            indicator.setDrawablePadding(GraphicUtils.convertDpToPixel(5));
            tabHost.addTab(tabSpec);
        }
    }

    private int getTabIconRes(int position) {
        if (User.isMe(userId)) {
            if (position == 0)
                return R.drawable.icon_profile_tab_album;
            if (position == 1)
                return R.drawable.icon_profile_tab_star;
            if (position == 2)
                return R.drawable.icon_profile_tab_media;
        }

        if (position == 0)
            return R.drawable.icon_profile_tab_album;
        if (position == 1)
            return R.drawable.icon_profile_tab_media;

        return 0;
    }

    private String getTabTitle(ApiUsers.Counts counts, int position) {
        int count = 0;

        if (User.isMe(userId)) {
            if (position == 0)
                count = counts.album;
            else if (position == 1)
                count = counts.favorites;
            else
                count = counts.media;
        } else {
            if (position == 0)
                count = counts.album;
            else
                count = counts.media;
        }

        return count > 0 ? String.valueOf(count) : null;
    }

    private void loadCount() {
        if (userId < 1)
            return;

        UserDataProxy.getDefault().count(userId, new Callback<ApiUsers.CountRes>() {
            @Override
            public void onResponse(Response<ApiUsers.CountRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    for (int i=0; i<tabHost.getTabWidget().getChildCount(); i++) {
                        PSButton button = (PSButton) tabHost.getTabWidget().getChildAt(i);
                        button.setText(getTabTitle(response.body().entity, i));
                    }
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);
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
        public int getCount() {
            return dataSource != null ? dataSource.getFragments().size() : 0;
        }

        @Override
        public Fragment getItem(int position) {
            try {
                Class clazz = dataSource.getFragments().get(position);
                UserDataGridFragment fragment = (UserDataGridFragment) clazz.newInstance();

                if (fragment != null) {
                    fragment.setUserId(userId);
                }

                return fragment;
            } catch (Exception e) {
                return null;
            }
        }
    }

    // ================================================================================================
    //  Interface: DataSource
    // ================================================================================================

    public static interface DataSource {
        List<Class<? extends UserDataGridFragment>> getFragments();
        FragmentManager getGridFragmentManager();
        int getTabCount();
    }
}
