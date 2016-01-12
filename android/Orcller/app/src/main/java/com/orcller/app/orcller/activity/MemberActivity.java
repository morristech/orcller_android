package com.orcller.app.orcller.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.facade.ApplicationFacade;
import com.orcller.app.orcller.fragment.MemberJoinFragment;
import com.orcller.app.orcller.fragment.MemberLoginFragment;
import pisces.psuikit.event.SoftKeyboardEvent;
import pisces.psuikit.keyboard.SoftKeyboardNotifier;
import pisces.psuikit.keyboard.SoftKeyboardUtils;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.ext.PSFragmentActivity;

/**
 * Created by pisces on 11/6/15.
 */
public class MemberActivity extends PSFragmentActivity {
    private LinearLayout container;
    private RelativeLayout titleContainer;
    private LinearLayout titleLinear;
    private FragmentTabHost tabHost;
    private ViewPager viewPager;
    private ImageView subtitleImageView;

    // ================================================================================================
    //  Overridden: FragmentActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_member);

        MemberFragmentPagerAdapter pagerAdapter = new MemberFragmentPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(pagerAdapter);

        tabHost = (FragmentTabHost) findViewById(R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.tabcontent);

        for (int i=0; i<pagerAdapter.getCount(); i++) {
            String name = (String) pagerAdapter.getPageTitle(i);
            String tag = String.valueOf(i);
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag).setIndicator(getTabIndicator(this, name));
            tabHost.addTab(tabSpec, Fragment.class, null);
        }

        container = (LinearLayout) findViewById(R.id.container);
        titleContainer = (RelativeLayout) findViewById(R.id.titleContainer);
        titleLinear = (LinearLayout) findViewById(R.id.titleLinear);
        subtitleImageView = (ImageView) findViewById(R.id.subtitleImageView);

        setStausBarColor();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SoftKeyboardNotifier.getDefault().unregister(this);
        EventBus.getDefault().unregister(this);
        tabHost.setOnTabChangedListener(null);
        viewPager.setOnPageChangeListener(null);
        container.setOnTouchListener(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        ApplicationFacade.clear();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void animateTitleContainer(int dimen) {
        Integer value = Math.round(getResources().getDimension(dimen));
        ValueAnimator animator = ValueAnimator.ofInt(titleContainer.getHeight(), value);
        animator.setDuration(250);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                titleContainer.getLayoutParams().height = value.intValue();
                titleContainer.requestLayout();
            }
        });
        animator.start();
    }

    private void animateTitleLiner(int dimen) {
        Integer value = Math.round(getResources().getDimension(dimen));
        final LinearLayout.MarginLayoutParams params = (LinearLayout.MarginLayoutParams) titleLinear.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(params.bottomMargin, value);
        animator.setDuration(250);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                params.bottomMargin = value;
                titleLinear.setLayoutParams(params);
            }
        });
        animator.start();
    }

    private View getTabIndicator(Context context, String title) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_member, null);
        TextView tv = (TextView) view.findViewById(R.id.textView);
        tv.setText(title);
        return view;
    }

    private void setListeners() {
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                viewPager.setCurrentItem(Integer.valueOf(tabId));
                SoftKeyboardUtils.hide(container);
            }
        });
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                tabHost.setCurrentTab(position);
                SoftKeyboardUtils.hide(container);
            }
        });
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    SoftKeyboardUtils.hide(v);
                return false;
            }
        });

        SoftKeyboardNotifier.getDefault().register(this);
        EventBus.getDefault().register(this);
    }

    private void setStausBarColor() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.theme_purple_primary));
        }
    }

    // ================================================================================================
    //  Listeners
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) subtitleImageView.getLayoutParams();

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                animateTitleContainer(R.dimen.member_title_height_selected);
                animateTitleLiner(R.dimen.member_title_margin_bottom_selected);
                titleLinear.setOrientation(LinearLayout.HORIZONTAL);

                params.topMargin = GraphicUtils.convertDpToPixel(5);
                params.leftMargin = GraphicUtils.convertDpToPixel(10);
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                animateTitleContainer(R.dimen.member_title_height_normal);
                animateTitleLiner(R.dimen.member_title_margin_bottom_normal);
                titleLinear.setOrientation(LinearLayout.VERTICAL);

                params.topMargin = GraphicUtils.convertDpToPixel(15);
                params.leftMargin = 0;
            }

            subtitleImageView.setLayoutParams(params);
        }
    }

    // ================================================================================================
    //  Class: MemberFragmentPagerAdapter
    // ================================================================================================

    public class MemberFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        final private int tabTitles[] = new int[]{R.string.w_sign_up, R.string.w_login_up};
        private FragmentManager fm;

        public MemberFragmentPagerAdapter(FragmentManager fm) {
            super(fm);

            this.fm = fm;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return new MemberJoinFragment();
            if (position == 1)
                return new MemberLoginFragment();
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Application.applicationContext().getResources().getString(tabTitles[position]);
        }
    }
}