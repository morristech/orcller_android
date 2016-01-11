package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.model.api.ApiRelationships;
import com.orcller.app.orcller.proxy.RelationshipsDataProxy;
import com.orcller.app.orcller.widget.UserListView;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import pisces.psfoundation.ext.Application;
import pisces.psuikit.widget.ExceptionView;
import retrofit.Call;

/**
 * Created by pisces on 12/12/15.
 */
public class FollowersActivity extends UserListActivity {

    // ================================================================================================
    //  Overridden: UserListActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.w_followers));
        exceptionViewManager.add(0, ExceptionViewFactory.create(ExceptionViewFactory.Type.NoFollowers, container));
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NoFollowers.equals(view.getTag()))
            return loadError == null && userListView.getItems().size() < 1;
        return super.shouldShowExceptionView(view);
    }

    @Override
    protected UserListView.DataSource createDataSource() {
        return new UserListView.DataSource<ApiRelationships.UserListRes>() {
            @Override
            public boolean followButtonHidden() {
                return false;
            }

            @Override
            public Call<ApiRelationships.UserListRes> createDataLoadCall(int limit, String after) {
                return RelationshipsDataProxy.getDefault().service().followers(userId);
            }

            @Override
            public AbstractDataProxy createDataProxy() {
                return RelationshipsDataProxy.getDefault();
            }
        };
    }

    @Override
    public void onLoadComplete(UserListView listView, ListEntity listEntity) {
        super.onLoadComplete(listView, listEntity);

        String countString = listEntity.count > 0 ? " " + String.valueOf(listEntity.count) : "";
        getSupportActionBar().setTitle(getString(R.string.w_followers) + countString);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(long userId) {
        Intent intent = new Intent(Application.applicationContext(), FollowersActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        Application.getTopActivity().startActivity(intent);
    }
}