package com.orcller.app.orcller.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.MediaListActivity;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.itemview.MediaGridItemView;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.UserDataGridView;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.model.Model;
import pisces.psuikit.event.IndexChangeEvent;
import retrofit.Call;

/**
 * Created by pisces on 12/11/15.
 */
public class UserMediaGridFragment extends UserDataGridFragment {
    public UserMediaGridFragment() {
        super();
    }

    public UserMediaGridFragment(long userId) {
        super(userId);
    }

    // ================================================================================================
    //  Overridden: UserDataGridFragment
    // ================================================================================================

    @Override
    protected void setUpViews(View view) {
        super.setUpViews(view);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected UserDataGridView.DataSource createDataSource() {
        if (getUserId() < 1)
            return null;

        return new UserDataGridView.DataSource<ApiUsers.AlbumListRes>() {
            @Override
            public Call createDataLoadCall(int limit, String after) {
                return UserDataProxy.getDefault().service().media(getUserId());
            }
        };
    }

    @Override
    protected Class getItemViewClass() {
        return MediaGridItemView.class;
    }

    @Override
    public void onSelect(UserDataGridView gridView, int position, Model item) {
        MediaListActivity.show(gridView.getItems(), position);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof IndexChangeEvent) {
            IndexChangeEvent casted = (IndexChangeEvent) event;

            if (casted.getTarget() instanceof MediaListActivity)
                gridView.setSelection(casted.getSelectedIndex());
        }
    }
}
