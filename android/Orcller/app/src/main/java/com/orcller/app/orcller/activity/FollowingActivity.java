package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.ListEntity;
import com.orcller.app.orcller.model.api.ApiRelationships;
import com.orcller.app.orcller.proxy.RelationshipsDataProxy;
import com.orcller.app.orcller.widget.UserListView;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import retrofit.Call;

/**
 * Created by pisces on 12/12/15.
 */
public class FollowingActivity extends UserListActivity {

    // ================================================================================================
    //  Overridden: UserListActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (User.isMe(userId))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
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
                return RelationshipsDataProxy.getDefault().service().following(userId);
            }
        };
    }

    @Override
    public void onLoadComplete(UserListView listView, ListEntity listEntity) {
        super.onLoadComplete(listView, listEntity);

        String countString = listEntity.count > 0 ? " " + String.valueOf(listEntity.count) : "";
        getSupportActionBar().setTitle(getString(R.string.w_following) + countString);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(long userId) {
        Intent intent = new Intent(Application.applicationContext(), FollowingActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof Model.Event) {
            Model.Event casted = (Model.Event) event;

            if (Model.Event.SYNCHRONIZE.equals(casted.getType()) &&
                    casted.getTarget().equals(AuthenticationCenter.getDefault().getUser())) {
                int count = AuthenticationCenter.getDefault().getUser().user_options.follow_count;
                String countString = count > 0 ? " " + String.valueOf(count) : "";
                getSupportActionBar().setTitle(getString(R.string.w_following) + countString);
            }
        }
    }
}
