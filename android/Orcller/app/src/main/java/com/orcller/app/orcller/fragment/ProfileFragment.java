package com.orcller.app.orcller.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.widget.ProfileContentView;
import com.orcller.app.orcller.widget.ProfileHearderView;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import java.util.Arrays;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFragment;

/**
 * Created by pisces on 11/3/15.
 */
public class ProfileFragment extends MainTabFragment
        implements ProfileHearderView.Delegate, ProfileContentView.DataSource {
    private User model;
    private ProfileHearderView profileHearderView;
    private ProfileContentView profileContentView;
    private List<Class<? extends UserDataGridFragment>> fragments;

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
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragments = createFragments();
        profileHearderView = (ProfileHearderView) view.findViewById(R.id.profileHearderView);
        profileContentView = (ProfileContentView) view.findViewById(R.id.profileContentView);
        profileHearderView.setDelegate(this);
        profileContentView.setDataSource(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int res = profileHearderView.isEditing() ? R.menu.menu_profile_save : R.menu.menu_profile;
        inflater.inflate(res, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                profileHearderView.save();
                return true;

            case R.id.options:
                //TODO: open options activity
                return true;
        }
        return getActivity().onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    protected void resumeFragment() {
        if (isViewCreated())
            modelChanged();
        else
            reload();
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

    /**
     * ProfileContentView.DataSource
     */
    public List<Class<? extends UserDataGridFragment>> getFragments() {
        return fragments;
    }

    public FragmentManager getGridFragmentManager() {
        return getChildFragmentManager();
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

        modelChanged();
    }

    private List<Class<? extends UserDataGridFragment>> createFragments() {
        return Arrays.asList(UserAlbumGridFragment.class, UserAlbumStarGridFragment.class, UserMediaGridFragment.class);
    }

    private void modelChanged() {
        profileHearderView.setModel(model);
        profileHearderView.setVisibility(View.VISIBLE);
        profileContentView.setVisibility(View.VISIBLE);
        profileContentView.setUserId(model.user_uid);
    }

    private void reload() {
        profileHearderView.setVisibility(View.VISIBLE);
        profileContentView.setVisibility(View.VISIBLE);
        profileContentView.reload();
    }
}
