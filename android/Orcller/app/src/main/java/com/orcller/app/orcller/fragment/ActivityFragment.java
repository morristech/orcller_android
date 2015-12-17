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

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.AlbumCreateActivity;
import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.activity.CoeditViewActivity;
import com.orcller.app.orcller.activity.PageListActivity;
import com.orcller.app.orcller.itemview.ActivityItemView;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Notification;
import com.orcller.app.orcller.model.api.ApiNotification;
import com.orcller.app.orcller.proxy.ActivityDataProxy;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSListView;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/3/15.
 */
public class ActivityFragment extends MainTabFragment
        implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int LIST_COUNT = 20;
    private boolean isViewCreated;
    private List<Notification> items = new ArrayList<>();
    private ApiNotification.NotificationList lastEntity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListAdapter listAdapter;
    private PSListView listView;

    public ActivityFragment() {
        super();
    }

    // ================================================================================================
    //  Overridden: MainTabFragment
    // ================================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        listView = (PSListView) view.findViewById(R.id.listView);
        listAdapter = new ListAdapter(getContext());

        swipeRefreshLayout.setOnRefreshListener(this);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
        ProgressBarManager.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide();

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void resumeFragment() {
    }

    @Override
    protected void startFragment() {
        reload();
    }

    // ================================================================================================
    //  Interface Implemetaion
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
    }

    /**
     * AdapterView.OnItemClickListener
     */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Notification model = items.get(position);

        if (Notification.Type.PageComment.equals(model.type) ||
                Notification.Type.PageComment.equals(model.type)) {
            PageListActivity.show(model.content_id);
        } else if (Notification.Type.CoeditingAccept.equals(model.type) ||
                Notification.Type.CoeditingAsk.equals(model.type) ||
                Notification.Type.CoeditingCancelAsk.equals(model.type) ||
                Notification.Type.CoeditingCancelInvite.equals(model.type) ||
                Notification.Type.CoeditingInvite.equals(model.type)) {
            CoeditViewActivity.show(model.content_id);
        } else if (!Notification.Type.Follow.equals(model.type)) {
            AlbumViewActivity.show(model.content_id, false);
        }
    }

    /**
     * SwipeRefreshLayout.OnRefreshListener
     */
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

        ActivityDataProxy.getDefault().list(LIST_COUNT, after, new Callback<ApiNotification.NotificationListRes>() {
            @Override
            public void onResponse(final Response<ApiNotification.NotificationListRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    if (after == null)
                        items.clear();

                    lastEntity = response.body().entity;

                    items.addAll(lastEntity.data);
                    listAdapter.notifyDataSetChanged();
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());
                }

                endDataLoading();
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
        public Notification getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ActivityItemView itemView;

            if (convertView == null) {
                itemView = new ActivityItemView(context);
                convertView = itemView;
            } else {
                itemView = (ActivityItemView) convertView;
            }

            itemView.setModel(getItem(position));
            itemView.setSeparatorVisibility(position < items.size() - 1);
            loadMore(position);

            return convertView;
        }
    }
}