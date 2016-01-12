package com.orcller.app.orcller.fragment;

import android.view.View;

import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.UserDataGridView;

import de.greenrobot.event.EventBus;
import pisces.psuikit.widget.ExceptionView;
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

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NoStar.equals(view.getTag()))
            return loadError == null && gridView.getItems().size() < 1;
        return super.shouldShowExceptionView(view);
    }

    @Override
    protected void modelChanged() {
        super.modelChanged();

        if (exceptionViewManager.getViewCount() == 3)
            exceptionViewManager.remove(0);

        exceptionViewManager.add(0, ExceptionViewFactory.create(ExceptionViewFactory.Type.NoStar, container));
    }
}
