package com.orcller.app.orcller.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.widget.UserListView;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Resources;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 12/12/15.
 */
abstract public class UserListActivity extends PSActionBarActivity implements UserListView.Delegate {
    protected static final String USER_ID_KEY = "userId";
    protected long userId;
    protected Error loadError;
    protected FrameLayout container;
    protected UserListView userListView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_list);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        container = (FrameLayout) findViewById(R.id.container);
        userListView = (UserListView) findViewById(R.id.userListView);
        userId = getIntent().getLongExtra(USER_ID_KEY, 0);

        exceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, container));
        userListView.setDelegate(this);
        userListView.setDataSource(createDataSource());
    }

    @Override
    public void onClick(ExceptionView view) {
        if (ExceptionViewFactory.Type.NetworkError.equals(view.getTag()) ||
                (ExceptionViewFactory.Type.UnknownError.equals(view.getTag()))) {
            exceptionViewManager.clear();
            reload();
        }
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NetworkError.equals(view.getTag())) {
            if (Application.isNetworkConnected())
                return false;
            if (userListView.getItems().size() > 0) {
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
    //  Listener
    // ================================================================================================

    /**
     * UserListView.Delgate
     */
    public void onFailure(UserListView listView, Error error) {
        loadError = userListView.getItems().size() < 1 ? error : null;

        ProgressBarManager.hide();
        exceptionViewManager.validate();
    }

    public void onLoad(UserListView listView) {
        loadError = null;

        if (listView.isFirstLoading())
            ProgressBarManager.show();
    }

    public void onLoadComplete(UserListView listView, ListEntity listEntity) {
        ProgressBarManager.hide();
        exceptionViewManager.validate();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    /**
     * @abstract
     */
    abstract protected UserListView.DataSource createDataSource();

    protected void reload() {
        userListView.reload();
    }
}