package com.orcller.app.orcller.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.widget.UserDataGridView;

import pisces.psfoundation.model.Model;
import pisces.psuikit.ext.PSFragment;

/**
 * Created by pisces on 12/11/15.
 */
abstract public class UserDataGridFragment extends PSFragment
        implements UserDataGridView.Delegate {
    private long userId;
    private Delegate delegate;
    private ProgressBar progressBar;
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
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        gridView = (UserDataGridView) view.findViewById(R.id.gridView);

        gridView.setItemViewClass(getItemViewClass());
        gridView.setDataSource(createDataSource());
        gridView.setDelegate(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        setDelegate(null);
        gridView = null;
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

        if (gridView != null)
            gridView.reload();
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

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * UserDataGridView.Delegate
     */
    public void onLoad(UserDataGridView gridView) {
        if (gridView.isFirstLoading())
            progressBar.setVisibility(View.VISIBLE);
    }

    public void onLoadComplete(UserDataGridView gridView, ListEntity listEntity) {
        progressBar.setVisibility(View.GONE);
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

    public static interface Delegate {
        void onScroll(AbsListView view, Point scrollPoint);
        void onScrollStateChanged(AbsListView view, int scrollState);
    }
}