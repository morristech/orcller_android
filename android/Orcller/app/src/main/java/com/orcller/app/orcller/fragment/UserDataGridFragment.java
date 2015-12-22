package com.orcller.app.orcller.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.widget.UserDataGridView;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psuikit.ext.PSFragment;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 12/11/15.
 */
abstract public class UserDataGridFragment extends PSFragment
        implements SwipeRefreshLayout.OnRefreshListener, UserDataGridView.Delegate {
    private boolean userIdChanged;
    private long userId;
    private Delegate delegate;
    private SwipeRefreshLayout swipeRefreshLayout;
    protected Error loadError;
    protected FrameLayout container;
    protected UserDataGridView gridView;

    public UserDataGridFragment() {
    }

    // ================================================================================================
    //  Overridden: PSFragment
    // ================================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_data_grid, null);
    }

    @Override
    protected void commitProperties() {
        if (userIdChanged) {
            userIdChanged = false;
            userIdChanged();
        }
    }

    @Override
    protected void setUpSubviews(View view) {
        super.setUpSubviews(view);

        container = (FrameLayout) view.findViewById(R.id.container);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        gridView = (UserDataGridView) view.findViewById(R.id.gridView);

        exceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, container));
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_purple_accent);
        swipeRefreshLayout.setOnRefreshListener(this);
        gridView.setItemViewClass(getItemViewClass());
        gridView.setDataSource(createDataSource());
        gridView.setDelegate(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ProgressBarManager.hide(container);

        userIdChanged = true;
        container = null;
        gridView = null;
    }

    @Override
    public void onClick(ExceptionView view) {
        if (gridView.getItems().size() > 1)
            gridView.reload();
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NetworkError.equals(view.getTag()))
            return !Application.isNetworkConnected();
        if (ExceptionViewFactory.Type.UnknownError.equals(view.getTag()))
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

        invalidateProperties();
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

    protected void reset() {
        userIdChanged = true;

        gridView.cancel();
        invalidateProperties();
    }

    protected void userIdChanged() {
        gridView.reload();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * SwipeRefreshLayout.OnRefreshListener
     */
    public void onRefresh() {
        userIdChanged();
    }

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