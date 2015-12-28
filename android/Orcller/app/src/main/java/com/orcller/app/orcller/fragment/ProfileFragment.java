package com.orcller.app.orcller.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.OptionsActivity;
import com.orcller.app.orcller.widget.ProfileContentView;
import com.orcller.app.orcller.widget.ProfileHearderView;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;

import java.util.Arrays;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.ObjectUtils;

/**
 * Created by pisces on 11/3/15.
 */
public class ProfileFragment extends MainTabFragment
        implements ProfileHearderView.Delegate, ProfileContentView.DataSource {
    private boolean modelChanged;
    private List<Class<? extends UserDataGridFragment>> fragments;
    private User model;
    private ProfileHearderView profileHearderView;
    private ProfileContentView profileContentView;

    public ProfileFragment() {
        super();
    }

    // ================================================================================================
    //  Overridden: MainTabFragment
    // ================================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, null);
    }

    @Override
    protected void setUpSubviews(View view) {
        super.setUpSubviews(view);

        fragments = createFragments();
        profileHearderView = (ProfileHearderView) view.findViewById(R.id.profileHearderView);
        profileContentView = (ProfileContentView) view.findViewById(R.id.profileContentView);
        profileHearderView.setDelegate(this);
        profileContentView.setDataSource(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int res = profileHearderView.isEditing() ? R.menu.menu_save : R.menu.menu_profile;
        inflater.inflate(res, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                profileHearderView.save();
                return true;

            case R.id.options:
                Application.startActivity(OptionsActivity.class);
                return true;
        }
        return getActivity().onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public String getToolbarTitle() {
        return AuthenticationCenter.getDefault().hasSession() ?
                AuthenticationCenter.getDefault().getUser().user_id : "";
    }

    @Override
    public boolean isUseSoftKeyboard() {
        return true;
    }

    @Override
    protected void commitProperties() {
        super.commitProperties();

        if (modelChanged) {
            modelChanged = false;
            modelChanged();
        }
    }

    @Override
    protected void startFragment() {
        setModel(AuthenticationCenter.getDefault().getUser());
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * ProfileHearderView.Delegate
     */
    public void onChangeState() {
        getActivity().invalidateOptionsMenu();
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
        return getChildFragmentManager();
    }

    public UserDataGridFragment.Delegate getUserDataGridFragmentDelegate() {
        return null;
    }

    public int getTabCount() {
        return fragments.size();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private User getModel() {
        return model;
    }

    private void setModel(User model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;
        modelChanged = true;

        invalidateProperties();
    }

    private List<Class<? extends UserDataGridFragment>> createFragments() {
        return Arrays.asList(UserAlbumGridFragment.class, UserAlbumStarGridFragment.class, UserMediaGridFragment.class);
    }

    private void modelChanged() {
        profileHearderView.setModel(model);
        profileHearderView.setVisibility(View.VISIBLE);
        profileContentView.setVisibility(View.VISIBLE);
        profileContentView.setModel(model);
    }
}
