package com.orcller.app.orcller.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.IdpListItemView;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.model.api.ApiRelationships;
import com.orcller.app.orcller.proxy.RelationshipsDataProxy;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.UserListView;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import de.greenrobot.event.EventBus;
import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Resources;
import pisces.psuikit.event.SoftKeyboardEvent;
import pisces.psuikit.ext.PSView;
import pisces.psuikit.itemview.ListBaseItemView;
import pisces.psuikit.manager.ExceptionViewManager;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.ExceptionView;
import retrofit.Call;

/**
 * Created by pisces on 11/3/15.
 */
public class FindFriendsFragment extends MainTabFragment
        implements SearchView.OnCloseListener, SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, UserListView.Delegate {
    private Error loadError;
    private ExceptionViewManager searchExceptionViewManager;
    private LinearLayout firstViewContainer;
    private FrameLayout secondViewContainer;
    private FrameLayout userListViewContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private UserListView searchListView;
    private UserListView userListView;
    private IdpListItemView facebookItemView;

    public FindFriendsFragment() {
        super();
    }

    // ================================================================================================
    //  Overridden: MainTabFragment
    // ================================================================================================

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_findfrineds, null);
    }

    @Override
    protected void setUpSubviews(View view) {
        super.setUpSubviews(view);

        firstViewContainer = (LinearLayout) view.findViewById(R.id.firstViewContainer);
        secondViewContainer = (FrameLayout) view.findViewById(R.id.secondViewContainer);
        userListViewContainer = (FrameLayout) view.findViewById(R.id.userListViewContainer);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        searchListView = (UserListView) view.findViewById(R.id.searchListView);
        userListView = (UserListView) view.findViewById(R.id.userListView);
        facebookItemView = (IdpListItemView) view.findViewById(R.id.facebookItemView);
        searchView = new SearchView(getContext());
        searchExceptionViewManager = new ExceptionViewManager(this);
        View headerView = new View(getContext());
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        searchEditText.setHintTextColor(Resources.getColor(R.color.background_button_purple_disabled));
        searchEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setTextSize(13);
        facebookItemView.setImageDrawable(getResources().getDrawable(R.drawable.icon_facebook_normal));
        facebookItemView.setLineDirection(ListBaseItemView.LINE_BOTTOM);
        facebookItemView.setTitleText(R.string.w_invite_facebook);
        exceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NoRecommendation, userListViewContainer),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, userListViewContainer),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, userListViewContainer));
        searchExceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NoListData, secondViewContainer),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, secondViewContainer),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, secondViewContainer));
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_purple_accent);
        swipeRefreshLayout.setOnRefreshListener(this);
        searchListView.setDelegate(this);
        userListView.addHeaderView(headerView);
        userListView.setDelegate(this);
        searchView.setOnSearchClickListener(this);
        searchView.setOnCloseListener(this);
        searchView.setQueryHint(getString(R.string.m_hint_search_friend));
        searchView.setOnQueryTextListener(this);
        facebookItemView.setOnClickListener(this);
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
        return Resources.getString(R.string.w_title_find_friends);
    }

    @Override
    public boolean isUseSoftKeyboard() {
        return true;
    }

    @Override
    protected void startFragment() {
        load();
    }

    @Override
    public void onClick(ExceptionView view) {
        if (PSView.isShown(secondViewContainer)) {
            searchListView.reload();
        } else {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            userListView.reload();
        }
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (PSView.isShown(secondViewContainer)) {
            int index = searchExceptionViewManager.getViewIndex(view);
            if (index == 0)
                return loadError == null && searchListView.getItems().size() < 1;
            if (index == 1) {
                if (Application.isNetworkConnected())
                    return false;
                if (searchListView.getItems().size() > 0) {
                    Toast.makeText(
                            Application.getTopActivity(),
                            Resources.getString(R.string.m_exception_title_error_network_long),
                            Toast.LENGTH_LONG)
                            .show();
                    return false;
                }
                return true;
            }
            if (index == 2)
                return loadError != null;
            return false;
        }

        int index = exceptionViewManager.getViewIndex(view);
        if (index == 0)
            return loadError == null && userListView.getItems().size() < 1;

        if (index == 1) {
            if (Application.isNetworkConnected())
                return false;
            if (userListView.getItems().size() > 0) {
                Toast.makeText(
                        Application.getTopActivity(),
                        Resources.getString(R.string.m_exception_title_error_network_long),
                        Toast.LENGTH_LONG)
                        .show();
                return false;
            }
            return true;
        }
        if (index == 2)
            return loadError != null;
        return false;
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
                searchView.setQuery("", false);
                searchView.setIconified(true);
                getActionBar().setDisplayShowTitleEnabled(true);
            }
        }
    }

    /**
     * View.OnClickListener
     */
    public void onClick(View v) {
        if (facebookItemView.equals(v)) {
            inviteFacebookFriends();
        } else {
            firstViewContainer.setVisibility(View.GONE);
            secondViewContainer.setVisibility(View.VISIBLE);
            getActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    /**
     * SwipeRefreshLayout.OnRefreshListener
     */
    public void onRefresh() {
        load();
    }

    /**
     * SearchView.OnCloseListener
     */
    public boolean onClose() {
        getActionBar().setDisplayShowTitleEnabled(true);
        searchListView.clear();
        searchExceptionViewManager.clear();
        secondViewContainer.setVisibility(View.GONE);
        firstViewContainer.setVisibility(View.VISIBLE);
        return false;
    }

    /**
     * SearchView.OnQueryTextListener
     */
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public boolean onQueryTextChange(String newText) {
        if (newText.trim().length() > 0) {
            searchListView.clear();
            searchExceptionViewManager.clear();
            search(newText.trim());
        }
        return false;
    }

    /**
     * UserListView.Delgate
     */
    public void onFailure(UserListView listView, Error error) {
        loadError = listView.getItems().size() < 1 ? error : null;
        onLoadComplete(listView, null);
    }

    public void onLoad(UserListView listView) {
        loadError = null;

        if (listView.equals(searchListView)) {
            ProgressBarManager.show(secondViewContainer);
        } else if (listView.equals(userListView) && listView.isFirstLoading()) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    public void onLoadComplete(UserListView listView, ListEntity listEntity) {
        if (listView.equals(searchListView)) {
            ProgressBarManager.hide(secondViewContainer);
            searchExceptionViewManager.validate();
        } else {
            ProgressBarManager.hide(userListViewContainer);
            exceptionViewManager.validate();
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void inviteFacebookFriends() {
        AppInviteContent content = new AppInviteContent.Builder()
                .setApplinkUrl(getString(R.string.facebook_invite_url))
                .setPreviewImageUrl(SharedObject.toFullMediaUrl(getString(R.string.facebook_invite_preview_image_path)))
                .build();
        AppInviteDialog.show(this, content);
    }

    private void load() {
        userListView.setDataSource(new UserListView.DataSource<ApiRelationships.RecommendRes>() {
            @Override
            public boolean followButtonHidden() {
                return false;
            }

            @Override
            public Call<ApiRelationships.RecommendRes> createDataLoadCall(int limit, String after) {
                return RelationshipsDataProxy.getDefault().service().recommends(
                        AccessToken.getCurrentAccessToken() != null ? AccessToken.getCurrentAccessToken().getToken() : null,
                        InstagramApplicationCenter.getDefault().getAccessToken());
            }

            @Override
            public AbstractDataProxy createDataProxy() {
                return RelationshipsDataProxy.getDefault();
            }
        });
    }

    private void search(final String keyword) {
        searchListView.setDataSource(new UserListView.DataSource<ApiRelationships.UserListRes>() {
            @Override
            public boolean followButtonHidden() {
                return false;
            }

            @Override
            public Call<ApiRelationships.UserListRes> createDataLoadCall(int limit, String after) {
                return UserDataProxy.getDefault().service().serach(keyword, 0, null);
            }

            @Override
            public AbstractDataProxy createDataProxy() {
                return UserDataProxy.getDefault();
            }
        });
    }
}
