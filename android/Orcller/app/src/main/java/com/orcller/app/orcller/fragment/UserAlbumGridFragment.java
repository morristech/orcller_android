package com.orcller.app.orcller.fragment;

import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.itemview.AlbumCoverGridItemView;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.UserDataGridView;

import pisces.psfoundation.model.Model;
import retrofit.Call;

/**
 * Created by pisces on 12/11/15.
 */
public class UserAlbumGridFragment extends UserDataGridFragment {
    public UserAlbumGridFragment() {
        super();
    }

    public UserAlbumGridFragment(long userId) {
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
                return UserDataProxy.getDefault().service().albums(getUserId());
            }
        };
    }

    @Override
    protected Class getItemViewClass() {
        return AlbumCoverGridItemView.class;
    }


    @Override
    public void onSelect(UserDataGridView gridView, int position, Model item) {
        AlbumViewActivity.show(((Album) item).id, false);
    }
}
