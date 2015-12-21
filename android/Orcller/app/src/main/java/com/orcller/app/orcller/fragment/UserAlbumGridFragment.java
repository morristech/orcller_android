package com.orcller.app.orcller.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.orcller.app.orcller.activity.AlbumCreateActivity;
import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.AlbumCoverGridItemView;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.UserDataGridView;
import com.orcller.app.orcllermodules.model.User;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psuikit.ext.PSView;
import pisces.psuikit.widget.ExceptionView;
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

    @Override
    protected void userIdChanged() {
//        if (exceptionViewManager.getViewCount() == 3)
//            exceptionViewManager.remove(0);
//
//        if (User.isMe(getUserId()))
//            exceptionViewManager.add(0, ExceptionViewFactory.create(ExceptionViewFactory.Type.NoAlbumMine, container));
//        else
//            exceptionViewManager.add(0, ExceptionViewFactory.create(ExceptionViewFactory.Type.NoAlbum, container));

        super.userIdChanged();
    }

    @Override
    public void onClick(ExceptionView view) {
        int index = exceptionViewManager.getViewIndex(view);

        if (index > 0)
            super.onClick(view);
        else if (User.isMe(getUserId()))
            AlbumCreateActivity.show();
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        int index = exceptionViewManager.getViewIndex(view);
        if (index == 0)
            return loadError == null && gridView.getItems().size() < 1;
        return super.shouldShowExceptionView(view);
    }
}
