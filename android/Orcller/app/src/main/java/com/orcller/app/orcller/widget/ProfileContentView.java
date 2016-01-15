package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.fragment.MainTabFragment;
import com.orcller.app.orcller.fragment.UserDataGridFragment;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.model.User;

import java.util.List;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSTabHost;
import pisces.psuikit.widget.PSButton;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/11/15.
 */
public class ProfileContentView extends PSTabHost
        implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    private boolean modelChanged;
    private User model;
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
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageMargin(GraphicUtils.convertDpToPixel(15));
        viewPager.setPageMarginDrawable(android.R.color.white);
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void commitProperties() {
        if (modelChanged) {
            modelChanged = false;

            addTabs();
            loadCount();
            viewPager.setAdapter(pagerAdapter);
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;

        pagerAdapter = new PagerAdapter(
                dataSource.getGridFragmentManager(), dataSource.getUserDataGridFragmentDelegate());
    }

    public User getModel() {
        return model;
    }

    public void setModel(User model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;
        modelChanged = true;

        invalidateProperties();
    }

    public void reload() {
        pagerAdapter.notifyDataSetChanged();
        loadCount();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

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
        viewPager.setCurrentItem(Integer.valueOf(tag), true);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void addTabs() {
        if (dataSource == null || model == null || tabHost.getTabWidget().getTabCount() > 0)
            return;

        for (int i=0; i<dataSource.getTabCount(); i++) {
            PSButton indicator = new PSButton(getContext());
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(String.valueOf(i))
                    .setIndicator(indicator)
                    .setContent(new TabFactory(getContext()));
            indicator.setBackgroundResource(R.drawable.background_tab_profile_content);
            indicator.setDrawableLeft(getTabIconRes(i));
            indicator.setDrawableBound(getTabIconBound(i));
            indicator.setDrawablePadding(GraphicUtils.convertDpToPixel(5));
            indicator.setTextColor(getResources().getColorStateList(R.drawable.color_tab_profile_content));
            indicator.setTextSize(GraphicUtils.convertDpToPixel(13));
            tabHost.addTab(tabSpec);
        }
    }

    private int getTabIconRes(int position) {
        if (model.isMe()) {
            if (position == 0)
                return R.drawable.icon_profile_tabbar_album;
            if (position == 1)
                return R.drawable.icon_profile_tabbar_star;
            if (position == 2)
                return R.drawable.icon_profile_tabbar_media;
        }

        if (position == 0)
            return R.drawable.icon_profile_tabbar_album;
        if (position == 1)
            return R.drawable.icon_profile_tabbar_media;

        return 0;
    }


    private Rect getTabIconBound(int position) {
        int resId = getTabIconRes(position);
        if (resId == R.drawable.icon_profile_tabbar_album)
            return new Rect(0, 0, GraphicUtils.convertDpToPixel(16), GraphicUtils.convertDpToPixel(16));
        if (resId == R.drawable.icon_profile_tabbar_star)
            return new Rect(0, 0, GraphicUtils.convertDpToPixel(17), GraphicUtils.convertDpToPixel(16));
        return new Rect(0, 0, GraphicUtils.convertDpToPixel(17.5f), GraphicUtils.convertDpToPixel(16));
    }

    private String getTabTitle(ApiUsers.Counts counts, int position) {
        int count = 0;

        if (model.isMe()) {
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
        if (model == null)
            return;

        UserDataProxy.getDefault().count(model.user_uid, new Callback<ApiUsers.CountRes>() {
            @Override
            public void onResponse(Response<ApiUsers.CountRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    for (int i=0; i<tabHost.getTabWidget().getChildCount(); i++) {
                        PSButton button = (PSButton) tabHost.getTabWidget().getChildAt(i);
                        button.setText(getTabTitle(response.body().entity, i));
                    }
                } else {
                    if (DEBUG)
                        e("Api Error", response.body());
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
        private UserDataGridFragment.Delegate delegate;

        public PagerAdapter(FragmentManager fm, UserDataGridFragment.Delegate delegate) {
            super(fm);

            this.delegate = delegate;
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

                fragment.setDelegate(delegate);
                fragment.setModel(model);

                return fragment;
            } catch (Exception e) {
                return null;
            }
        }
    }

    // ================================================================================================
    //  Interface: DataSource
    // ================================================================================================

    public interface DataSource {
        List<Class<? extends UserDataGridFragment>> getFragments();
        FragmentManager getGridFragmentManager();
        UserDataGridFragment.Delegate getUserDataGridFragmentDelegate();
        int getTabCount();
    }
}
