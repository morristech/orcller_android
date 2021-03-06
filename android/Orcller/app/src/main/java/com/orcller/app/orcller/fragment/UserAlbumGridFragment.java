package com.orcller.app.orcller.fragment;

import android.view.View;

import com.orcller.app.orcller.activity.AlbumCreateActivity;
import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.activity.CoeditInviteActivity;
import com.orcller.app.orcller.event.AlbumEvent;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.AlbumCoverGridItemView;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.UserDataGridView;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.widget.ExceptionView;
import retrofit.Call;

/**
 * Created by pisces on 12/11/15.
 */
public class UserAlbumGridFragment extends UserDataGridFragment {
    public UserAlbumGridFragment() {
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
        if (getModel() == null)
            return null;

        return new UserDataGridView.DataSource<ApiUsers.AlbumListRes>() {
            @Override
            public Call createDataLoadCall(int limit, String after) {
                return UserDataProxy.getDefault().service().albums(getModel().user_uid, limit, after);
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
    public void onClick(ExceptionView view) {
        if (ExceptionViewFactory.Type.NoAlbumCoedit.equals(view.getTag())) {
            CoeditInviteActivity.show(getModel());
        } else if (ExceptionViewFactory.Type.NoAlbumMine.equals(view.getTag())) {
            AlbumCreateActivity.show();
        } else {
            super.onClick(view);
        }
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NoAlbum.equals(view.getTag()) ||
                ExceptionViewFactory.Type.NoAlbumMine.equals(view.getTag()) ||
                ExceptionViewFactory.Type.NoAlbumCoedit.equals(view.getTag()))
            return loadError == null && gridView.getItems().size() < 1;
        return super.shouldShowExceptionView(view);
    }

    @Override
    protected void modelChanged() {
        if (exceptionViewManager.getViewCount() == 3)
            exceptionViewManager.remove(0);

        if (getModel().isMe())
            exceptionViewManager.add(0, ExceptionViewFactory.create(ExceptionViewFactory.Type.NoAlbumMine, container));
        else if (getModel().isFollower())
            exceptionViewManager.add(0, ExceptionViewFactory.create(ExceptionViewFactory.Type.NoAlbumCoedit, container));
        else
            exceptionViewManager.add(0, ExceptionViewFactory.create(ExceptionViewFactory.Type.NoAlbum, container));

        super.modelChanged();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof AlbumEvent) {
            AlbumEvent casted = (AlbumEvent) event;

            if (AlbumEvent.CREATE.equals(casted.getType()) ||
                    AlbumEvent.DELETE.equals(casted.getType()) ||
                    AlbumEvent.MODIFY.equals(casted.getType())) {
                modelChanged();
            }
        }
    }
}
