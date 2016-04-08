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
import com.orcller.app.orcller.utils.CustomSchemeGenerator;
import com.orcller.app.orcller.widget.FollowButton;
import com.orcller.app.orcller.widget.ProfileContentView;
import com.orcller.app.orcller.widget.ProfileHearderView;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.ApiUser;

import java.util.Arrays;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.keyboard.SoftKeyboardNotifier;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/10/15.
 */
public class ProfileActivity extends BaseActionBarActivity
        implements ProfileHearderView.Delegate, ProfileContentView.DataSource {
    private static final String USER_UID_KEY = "user_uid";
    private long userId;
    private User model;
    private ProfileHearderView profileHearderView;
    private ProfileContentView profileContentView;
    private List<Class<? extends UserDataGridFragment>> fragments;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        fragments = createFragments();
        profileHearderView = (ProfileHearderView) findViewById(R.id.profileHearderView);
        profileContentView = (ProfileContentView) findViewById(R.id.profileContentView);

        String userIdString = getIntent().getData() != null ? getIntent().getData().getQueryParameter(USER_UID_KEY) : null;
        userId = userIdString != null ? Long.valueOf(userIdString) : 0;

        if (User.isMe(userId)) {
            setModel(AuthenticationCenter.getDefault().getUser());
        } else {
            loadProfile();
        }

        profileHearderView.setDelegate(this);
        profileContentView.setDataSource(this);
        SoftKeyboardNotifier.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (model == null)
            return super.onCreateOptionsMenu(menu);

        if (model.isMe()) {
            int res = profileHearderView.isEditing() ? R.menu.menu_save : R.menu.menu_profile;
            Application.getTopActivity().getMenuInflater().inflate(res, menu);
            return true;
        }

        if (!model.isFollowing()) {
            Application.getTopActivity().getMenuInflater().inflate(R.menu.menu_follow, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.follow:
                FollowButton button = new FollowButton(this);
                button.setModel(model);
                button.onClick(button);
                invalidateOptionsMenu();
                return true;

            case R.id.options:
                Application.startActivity(OptionsActivity.class);
                return true;

            case R.id.save:
                profileHearderView.save();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected CustomSchemeGenerator.ViewInfo createViewInfo() {
        return new CustomSchemeGenerator.ViewInfo(
                CustomSchemeGenerator.Category.Users, CustomSchemeGenerator.ViewTypeUsers.Profile.value());
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

    public void onSyncModel() {
    }

    /**
     * ProfileContentView.DataSource
     */
    public List<Class<? extends UserDataGridFragment>> getFragments() {
        return fragments;
    }

    public FragmentManager getGridFragmentManager() {
        return getSupportFragmentManager();
    }

    public UserDataGridFragment.Delegate getUserDataGridFragmentDelegate() {
        return null;
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
        profileContentView.setModel(model);
        profileContentView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    protected List<Class<? extends UserDataGridFragment>> createFragments() {
        if (User.isMe(userId))
            return Arrays.asList(UserAlbumGridFragment.class, UserAlbumStarGridFragment.class, UserMediaGridFragment.class);
        return Arrays.asList(UserAlbumGridFragment.class, UserMediaGridFragment.class);
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
                    if (DEBUG)
                        e("Api Error", response.body());

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
        getSupportActionBar().setTitle(profileHearderView.isEditing() ? getString(R.string.w_title_edit_profile) : model.user_id);
    }
}
