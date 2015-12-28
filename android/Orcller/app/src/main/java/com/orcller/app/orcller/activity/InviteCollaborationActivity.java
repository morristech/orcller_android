package com.orcller.app.orcller.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.CoeditListItemView;
import com.orcller.app.orcller.model.AlbumCoedit;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.User;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.ExceptionView;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/28/15.
 */
public class InviteCollaborationActivity extends PSActionBarActivity implements AdapterView.OnItemClickListener {
    private static final String USER_KEY = "user";
    private static final int LOAD_LIMIT = 20;
    private List<AlbumCoedit> items = new ArrayList<>();
    private Error loadError;
    private User model;
    private ApiUsers.AlbumCoeditList lastEntity;
    private RelativeLayout container;
    private ListView listView;
    private ListAdapter listAdapter;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite_collaboration);
        setToolbar((Toolbar) findViewById(R.id.toolbar));

        container = (RelativeLayout) findViewById(R.id.container);
        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new ListAdapter(this);
        model = (User) getIntent().getSerializableExtra(USER_KEY);

        exceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NoAlbumMine, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, container));
        listView.setAdapter(listAdapter);

        String endfix = model != null ? " - " + model.user_name : "";
        getSupportActionBar().setTitle(getString(R.string.w_invite_collaboration) + endfix);
        load(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        listView = null;
        listAdapter = null;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide();
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NoAlbumMine.equals(view.getTag()))
            return loadError == null && items.size() < 1;

        if (ExceptionViewFactory.Type.NetworkError.equals(view.getTag())) {
            if (Application.isNetworkConnected())
                return false;
            if (items.size() > 0) {
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

    public static void show(User model) {
        Intent intent = new Intent(Application.applicationContext(), InviteCollaborationActivity.class);
        intent.putExtra(USER_KEY, model);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CoeditViewActivity.show(items.get(position));
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(final String after) {
        if (model == null || invalidDataLoading())
            return;

        if (isFirstLoading())
            ProgressBarManager.show();

        loadError = null;

        UserDataProxy.getDefault().albumsForInvite(model.user_uid, LOAD_LIMIT, after, new Callback<ApiUsers.AlbumCoeditListRes>() {
            @Override
            public void onResponse(final Response<ApiUsers.AlbumCoeditListRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    if (after == null)
                        items.clear();

                    lastEntity = response.body().entity;

                    items.addAll(lastEntity.data);
                    listAdapter.notifyDataSetChanged();
                    endDataLoading();
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());

                    loadError = items.size() < 1 ? new APIError(response.body()) : null;

                    endDataLoading();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                loadError = items.size() < 1 ? new Error(t.getMessage()) : null;

                endDataLoading();
            }
        });
    }

    private void loadAfter() {
        if (lastEntity != null && lastEntity.after != null)
            load(lastEntity.after);
    }

    private void loadMore(int position) {
        if (lastEntity != null &&
                items.size() < lastEntity.total_count &&
                position >= items.size() - 3)
            loadAfter();
    }

    // ================================================================================================
    //  Class: ListAdapter
    // ================================================================================================

    private class ListAdapter extends BaseAdapter {
        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return items != null ? items.size() : 0;
        }

        @Override
        public AlbumCoedit getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CoeditListItemView itemView;

            if (convertView == null) {
                itemView = new CoeditListItemView(context);
                convertView = itemView;
            } else {
                itemView = (CoeditListItemView) convertView;
            }

            itemView.setModel(getItem(position));
            loadMore(position);

            return convertView;
        }
    }
}
