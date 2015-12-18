package com.orcller.app.orcller.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.CoeditViewActivity;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.itemview.CoeditListItemView;
import com.orcller.app.orcller.model.Coedit;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.TimelineDataProxy;
import com.orcller.app.orcller.proxy.UserDataProxy;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.utils.Log;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * Created by pisces on 12/16/15.
 */
public class CoeditListFragment extends MainTabFragment
        implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int LOAD_LIMIT = 20;
    private List<Coedit> items = new ArrayList<>();
    private ApiUsers.CoeditList lastEntity;
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
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        listView = (ListView) view.findViewById(R.id.listView);
        listAdapter = new ListAdapter(getContext());

        swipeRefreshLayout.setColorSchemeResources(R.color.theme_lightgray_toolbar_control);
        swipeRefreshLayout.setOnRefreshListener(this);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        swipeRefreshLayout = null;
        listView = null;
        listAdapter = null;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide();

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void startFragment() {
        reload();
    }

    // ================================================================================================
    //  Interface Implemetaion
    // ================================================================================================

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CoeditViewActivity.show(items.get(position).id);
    }

    public void onRefresh() {
        reload();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(final String after) {
        if (invalidDataLoading())
            return;

        if (isFirstLoading())
            ProgressBarManager.show();

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

                    endDataLoading();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

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

    private void reload() {
        load(null);
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
