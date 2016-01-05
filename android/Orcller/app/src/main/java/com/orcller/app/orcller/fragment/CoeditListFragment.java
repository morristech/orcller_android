package com.orcller.app.orcller.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.CoeditViewActivity;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.event.AlbumEvent;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.CoeditListItemView;
import com.orcller.app.orcller.model.Coedit;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.error.APIError;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.widget.ExceptionView;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * Created by pisces on 12/16/15.
 */
public class CoeditListFragment extends MainTabFragment
        implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int LOAD_LIMIT = 20;
    private boolean shouldReloadData;
    private List<Coedit> items = new ArrayList<>();
    private Error loadError;
    private ApiUsers.CoeditList lastEntity;
    private FrameLayout container;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private ListAdapter listAdapter;

    // ================================================================================================
    //  Overridden: PSFragment
    // ================================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coedit_list, null);
    }

    @Override
    protected void setUpSubviews(View view) {
        super.setUpSubviews(view);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        container = (FrameLayout) view.findViewById(R.id.container);
        listView = (ListView) view.findViewById(R.id.listView);
        listAdapter = new ListAdapter(getContext());

        exceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NoCollaboration, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, container));
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_purple_accent);
        swipeRefreshLayout.setOnRefreshListener(this);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        invalidateLoading();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);

        swipeRefreshLayout = null;
        listView = null;
        listAdapter = null;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

        exceptionViewManager.validate();
    }

    @Override
    public String getToolbarTitle() {
        return Resources.getString(R.string.w_title_collaborations);
    }

    @Override
    protected void startFragment() {
        onRefresh();
    }

    @Override
    public void scrollToTop() {
        if (listView != null)
            listView.setSelection(0);
    }

    @Override
    public void onClick(ExceptionView view) {
        exceptionViewManager.clear();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        onRefresh();
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NoCollaboration.equals(view.getTag()))
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
    //  Interface Implementation
    // ================================================================================================

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CoeditViewActivity.show(items.get(position).id);
    }

    public void onRefresh() {
        shouldReloadData = true;
        invalidateLoading();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof AlbumEvent) {
            AlbumEvent casted = (AlbumEvent) event;

            if (!AlbumEvent.PREPARE.equals(casted.getType()))
                onRefresh();
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void invalidateLoading() {
        if (isActive() && shouldReloadData)
            load(null);
    }

    private void load(final String after) {
        if (!invalidDataLoading())
            return;

        if (isFirstLoading()) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        shouldReloadData = false;
        loadError = null;

        UserDataProxy.getDefault().coediting(LOAD_LIMIT, after, new Callback<ApiUsers.CoeditListRes>() {
            @Override
            public void onResponse(final Response<ApiUsers.CoeditListRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    if (after == null)
                        items.clear();

                    lastEntity = response.body().entity;

                    items.addAll(lastEntity.data);
                    listAdapter.notifyDataSetChanged();
                    endDataLoading();
                    UserDataProxy.getDefault().setLastViewDate(lastEntity.time);
                    SharedObject.get().setCoeditCount(0);
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
        public Coedit getItem(int position) {
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
