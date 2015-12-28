package com.orcller.app.orcller.fragment;

import android.view.View;

import com.orcller.app.orcller.activity.AlbumCreateActivity;
import com.orcller.app.orcller.activity.MediaListActivity;
import com.orcller.app.orcller.event.AlbumEvent;
import com.orcller.app.orcller.event.CoeditEvent;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.MediaGridItemView;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.UserDataGridView;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.model.Model;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.widget.ExceptionView;
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

        exceptionViewManager.add(0, ExceptionViewFactory.create(ExceptionViewFactory.Type.DoseNotExistPhoto, container));
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
                return UserDataProxy.getDefault().service().media(getModel().user_uid, limit, after);
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

    @Override
    public void onClick(ExceptionView view) {
        if (ExceptionViewFactory.Type.DoseNotExistPhoto.equals(view.getTag())) {
            AlbumCreateActivity.show();
        } else {
            super.onClick(view);
        }
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.DoseNotExistPhoto.equals(view.getTag()))
            return loadError == null && gridView.getItems().size() < 1;
        return super.shouldShowExceptionView(view);
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof IndexChangeEvent) {
            IndexChangeEvent casted = (IndexChangeEvent) event;

            if (casted.getTarget() instanceof MediaListActivity)
                gridView.setSelection(casted.getSelectedIndex());
        } else if (event instanceof CoeditEvent) {
            CoeditEvent casted = (CoeditEvent) event;

            if (CoeditEvent.CHANGE.equals(casted.getType()) &&
                    CoeditEvent.SYNC.equals(casted.getType()))
                reset();
        } else if (event instanceof AlbumEvent) {
            AlbumEvent casted = (AlbumEvent) event;

            if (AlbumEvent.CREATE.equals(casted.getType()) &&
                    AlbumEvent.CREATE.equals(casted.getType()) &&
                    AlbumEvent.MODIFY.equals(casted.getType()))
                reset();
        }
    }
}
