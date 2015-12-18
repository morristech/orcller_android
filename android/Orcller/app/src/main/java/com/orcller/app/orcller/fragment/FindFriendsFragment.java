package com.orcller.app.orcller.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.model.api.ApiRelationships;
import com.orcller.app.orcller.proxy.RelationshipsDataProxy;
import com.orcller.app.orcller.widget.UserListView;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import de.greenrobot.event.EventBus;
import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Call;

/**
 * Created by pisces on 11/3/15.
 */
public class FindFriendsFragment extends MainTabFragment
        implements SearchView.OnQueryTextListener, UserListView.Delegate {
    private UserListView userListView;
    private SearchView searchView;

    public FindFriendsFragment() {
        super();
    }

    // ================================================================================================
    //  Overridden: MainTabFragment
    // ================================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_findfrineds, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userListView = (UserListView) view.findViewById(R.id.userListView);
        searchView = new SearchView(getContext());
        int textViewId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(textViewId);

        userListView.setDelegate(this);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(13);
        textView.setHintTextColor(getContext().getResources().getColor(R.color.background_button_purple_disabled));
        searchView.setQueryHint(getString(R.string.m_hint_search_friend));
        searchView.setOnQueryTextListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_find_friends, menu);
        MenuItem item = menu.findItem(R.id.search);
        item.setActionView(searchView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.w_title_find_friends);
    }

    @Override
    public boolean isUseSoftKeyboard() {
        return true;
    }

    @Override
    protected void resumeFragment() {
        if (isViewCreated())
            load();
    }

    @Override
    protected void startFragment() {
        load();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                searchView.setIconified(true);
                searchView.setQuery("", false);
            }
        }
    }

    /**
     * SearchView.OnQueryTextListener
     */
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public boolean onQueryTextChange(String newText) {
        return false;
    }

    /**
     * UserListView.Delgate
     */
    public void onLoad(UserListView listView) {
        if (listView.isFirstLoading())
            ProgressBarManager.show();
    }

    public void onLoadComplete(UserListView listView, ListEntity listEntity) {
        ProgressBarManager.hide();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private UserListView.DataSource createDataSource() {
        return new UserListView.DataSource<ApiRelationships.RecommendRes>() {
            @Override
            public boolean followButtonHidden() {
                return true;
            }

            @Override
            public Call<ApiRelationships.RecommendRes> createDataLoadCall(int limit, String after) {
                return RelationshipsDataProxy.getDefault().service().recommends(
                        AccessToken.getCurrentAccessToken().getToken(),
                        InstagramApplicationCenter.getDefault().getAccessToken());
            }

            @Override
            public AbstractDataProxy createDataProxy() {
                return RelationshipsDataProxy.getDefault();
            }
        };
    }

    private void load() {
        userListView.setDataSource(createDataSource());
    }
}
