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

    public UserAlbumStarGridFragment(long userId) {
        super(userId);
    }

    // ================================================================================================
    //  Overridden: UserDataGridFragment
    // ================================================================================================

    @Override
    protected UserDataGridView.DataSource createDataSource() {
        if (getUserId() < 1)
            return null;

        return new UserDataGridView.DataSource<ApiUsers.AlbumListRes>() {
            @Override
            public Call createDataLoadCall(int limit, String after) {
                return UserDataProxy.getDefault().service().favorites(getUserId());
            }
        };
    }
}
