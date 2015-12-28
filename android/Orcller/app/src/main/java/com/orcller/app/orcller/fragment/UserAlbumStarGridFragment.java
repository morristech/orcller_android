package com.orcller.app.orcller.fragment;

import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.UserDataGridView;

import retrofit.Call;

/**
 * Created by pisces on 12/11/15.
 */
public class UserAlbumStarGridFragment extends UserAlbumGridFragment {
    public UserAlbumStarGridFragment() {
        super();
    }

    // ================================================================================================
    //  Overridden: UserDataGridFragment
    // ================================================================================================

    @Override
    protected UserDataGridView.DataSource createDataSource() {
        if (getModel() == null)
            return null;

        return new UserDataGridView.DataSource<ApiUsers.AlbumListRes>() {
            @Override
            public Call createDataLoadCall(int limit, String after) {
                return UserDataProxy.getDefault().service().favorites(getModel().user_uid, limit, after);
            }
        };
    }
}
