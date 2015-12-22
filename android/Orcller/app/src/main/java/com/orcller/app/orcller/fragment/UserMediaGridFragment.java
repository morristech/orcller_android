package com.orcller.app.orcller.fragment;

import android.view.View;

import com.orcller.app.orcller.activity.MediaListActivity;
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

    // ================================================================================================
    //  Overridden: UserDataGridFragment
    // ================================================================================================

    @Override
    protected void setUpSubviews(View view) {
        super.setUpSubviews(view);

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
