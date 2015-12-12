package com.orcller.app.orcller.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.fragment.UserAlbumGridFragment;
import com.orcller.app.orcller.fragment.UserAlbumStarGridFragment;
import com.orcller.app.orcller.fragment.UserDataGridFragment;
import com.orcller.app.orcller.fragment.UserMediaGridFragment;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.ProfileContentView;
import com.orcller.app.orcller.widget.ProfileHearderView;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.ApiUser;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import java.util.Arrays;
import java.util.List;

import pisces.psfoundation.ext.Application;
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
public class ProfileActivity extends PSActionBarActivity implements ProfileHearderView.Delegate, ProfileContentView.DataSource {
    private static final String USER_UID_KEY = "user_uid";
    private long userId;
    private User model;
    private ProfileHearderView profileHearderView;
    private ProfileContentView profileContentView;
    private List<UserDataGridFragment> fragments;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        userId = Long.valueOf(getIntent().getData().getQueryParameter(USER_UID_KEY));
        fragments = createFragments();
        profileHearderView = (ProfileHearderView) findViewById(R.id.profileHearderView);
        profileContentView = (ProfileContentView) findViewById(R.id.profileContentView);

        if (User.isMe(userId)) {
            setModel(AuthenticationCenter.getDefault().getUser());
        } else {
            loadProfile();
        }

        profileHearderView.setDelegate(this);
        profileContentView.setDataSource(this);
        profileContentView.setUserId(userId);
        SoftKeyboardNotifier.getDefault().register(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (User.isMe(userId)) {
            int res = profileHearderView.isEditing() ? R.menu.menu_profile_save : R.menu.menu_profile;
            Application.getTopActivity().getMenuInflater().inflate(res, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                profileHearderView.save();
                return false;

            case R.id.options:
                //TODO: open options activity
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SoftKeyboardNotifier.getDefault().unregister(this);

        profileHearderView = null;
        profileContentView = null;
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
     * ProfileHearderView.Delegate
     */
    public void onChangeState() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(!profileHearderView.isEditing());
        setToolbarTitle();
        invalidateOptionsMenu();
        profileContentView.setVisibility(profileHearderView.isEditing() ? View.GONE : View.VISIBLE);
    }

    /**
     * ProfileContentView.DataSource
     */
    public List<UserDataGridFragment> getFragments() {
        return fragments;
    }

    public FragmentManager getGridFragmentManager() {
        return getSupportFragmentManager();
    }

    public int getTabCount() {
        return fragments.size();
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

        setToolbarTitle();
        profileHearderView.setModel(model);
        profileHearderView.setVisibility(View.VISIBLE);
        profileContentView.setVisibility(View.VISIBLE);
    }

    protected List<UserDataGridFragment> createFragments() {
        if (User.isMe(userId))
            return Arrays.asList(new UserAlbumGridFragment(), new UserAlbumStarGridFragment(), new UserMediaGridFragment());
        return Arrays.asList(new UserAlbumGridFragment(), new UserMediaGridFragment());
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void loadProfile() {
        if (userId < 1 || invalidDataLoading())
            return;

        ProgressBarManager.show();

        UserDataProxy.getDefault().profile(userId, new Callback<ApiUser.Profile>() {
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

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(profileHearderView.isEditing() ? getString(R.string.w_title_profile) : model.user_id);
    }
}