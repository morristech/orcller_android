package com.orcller.app.orcller.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.orcller.app.orcller.AnalyticsTrackers;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.widget.UserDataGridView;
import com.orcller.app.orcllermodules.model.User;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFragment;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 12/11/15.
 */
abstract public class UserDataGridFragment extends PSFragment
        implements SwipeRefreshLayout.OnRefreshListener, UserDataGridView.Delegate {
    private boolean modelChanged;
    private boolean shouldReloadData;
    private User model;
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
        if (modelChanged) {
            modelChanged = false;
            modelChanged();
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
        AnalyticsTrackers.getInstance().trackScreen(AnalyticsTrackers.Target.APP, getClass().getName());
    }

    @Override
    public void onResume() {
        super.onResume();

        invalidateLoading();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        modelChanged = true;
    }

    @Override
    public void onClick(ExceptionView view) {
        if (ExceptionViewFactory.Type.NetworkError.equals(view.getTag()) ||
                (ExceptionViewFactory.Type.UnknownError.equals(view.getTag()))) {
            exceptionViewManager.clear();
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            gridView.reload();
        }
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NetworkError.equals(view.getTag())) {
            if (Application.isNetworkConnected())
                return false;
            if (gridView.getItems().size() > 0) {
                Toast.makeText(
                        Application.getTopActivity(),
                        Resources.getString(R.string.m_exception_title_error_network_long),
                        Toast.LENGTH_LONG)
                        .show();
                return false;
            }
            return true;
        }

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

    public User getModel() {
        return model;
    }

    public void setModel(User model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;
        modelChanged = true;

        invalidateProperties();
    }

    public void scrollToTop() {
        if (gridView != null)
            gridView.setSelection(0);
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

    protected void invalidateLoading() {
        if (isActive() && shouldReloadData) {
            shouldReloadData = false;
            gridView.reload();
        }
    }

    protected void modelChanged() {
        shouldReloadData = true;

        invalidateLoading();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * SwipeRefreshLayout.OnRefreshListener
     */
    public void onRefresh() {
        modelChanged();
    }

    /**
     * UserDataGridView.Delegate
     */
    public void onFailure(UserDataGridView gridView, Error error) {
        loadError = gridView.getItems().size() < 1 ? error : null;
        swipeRefreshLayout.setRefreshing(false);
        exceptionViewManager.validate();
    }

    public void onLoad(UserDataGridView gridView) {
        loadError = null;

        if (gridView.isFirstLoading()) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    public void onLoadComplete(UserDataGridView gridView, ListEntity listEntity) {
        swipeRefreshLayout.setRefreshing(false);
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