package com.orcller.app.orcller.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.widget.UserDataGridView;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSFragment;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 12/11/15.
 */
abstract public class UserDataGridFragment extends PSFragment
        implements UserDataGridView.Delegate {
    private boolean userIdChanged;
    private long userId;
    private Delegate delegate;
    protected Error loadError;
    protected FrameLayout container;
    protected UserDataGridView gridView;

    public UserDataGridFragment() {
    }

    public UserDataGridFragment(long userId) {
        this.userId = userId;
    }

    // ================================================================================================
    //  Overridden: PSFragment
    // ================================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_data_grid, null);
    }

    @Override
    protected void setUpViews(View view) {
        container = (FrameLayout) view.findViewById(R.id.container);
        gridView = (UserDataGridView) view.findViewById(R.id.gridView);

        exceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, container));
        gridView.setItemViewClass(getItemViewClass());
        gridView.setDataSource(createDataSource());
        gridView.setDelegate(this);
        userIdChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(ExceptionView view) {
        gridView.reload();
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (gridView.getItems().size() > 1)
            return false;

        int index = exceptionViewManager.getViewIndex(view);
        if (index == 0)
            return !Application.isNetworkConnected();
        if (index == 1)
            return loadError != null;
        return false;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        if (userId == this.userId)
            return;

        this.userId = userId;
        userIdChanged = true;

        userIdChanged();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    /**
     * @abstract
     */
    abstract protected UserDataGridView.DataSource createDataSource();

    /**
     * @abstract
     */
    abstract protected Class getItemViewClass();

    protected void reload() {
        if (gridView != null)
            gridView.reload();
    }

    protected void userIdChanged() {
        if (userIdChanged && gridView != null) {
            userIdChanged = false;
            reload();
        }
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * UserDataGridView.Delegate
     */
    public void onFailure(UserDataGridView gridView, Error error) {
        loadError = gridView.getItems().size() < 1 ? error : null;
        ProgressBarManager.hide(container);
        exceptionViewManager.validate();
    }

    public void onLoad(UserDataGridView gridView) {
        loadError = null;

        if (gridView.isFirstLoading())
            ProgressBarManager.show(container);
    }

    public void onLoadComplete(UserDataGridView gridView, ListEntity listEntity) {
        ProgressBarManager.hide(container);
        exceptionViewManager.validate();
    }

    public void onScroll(AbsListView view, Point scrollPoint) {
        if (delegate != null)
            delegate.onScroll(view, scrollPoint);
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (delegate != null)
            delegate.onScrollStateChanged(view, scrollState);
    }

    public void onSelect(UserDataGridView gridView, int position, Model item) {
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public interface Delegate {
        void onScroll(AbsListView view, Point scrollPoint);
        void onScrollStateChanged(AbsListView view, int scrollState);
    }
}