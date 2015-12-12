package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.UserItemView;
import com.orcller.app.orcller.model.album.ListEntity;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.model.BaseUser;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSListView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/10/15.
 */
public class UserListView extends PSListView {
    private int listCountAtOnce;
    private List<BaseUser> items = new ArrayList<>();
    private ListAdapter listAdapter;
    private DataSource dataSource;
    private Delegate delegate;
    private ListEntity lastEntity;

    public UserListView(Context context) {
        super(context);
    }

    public UserListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSListView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.UserDataGridView, defStyleAttr, defStyleRes);
        try {
            setListCountAtOnce(ta.getInt(R.styleable.UserDataGridView_listCountAtOnce, 20));
        } finally {
            ta.recycle();
        }

        listAdapter = new ListAdapter(context);

        setDivider(null);
        setAdapter(listAdapter);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        if (ObjectUtils.equals(dataSource, this.dataSource))
            return;

        this.dataSource = dataSource;

        load(null);
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public void setListCountAtOnce(int listCountAtOnce) {
        this.listCountAtOnce = listCountAtOnce;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(final String after) {
        if (invalidDataLoading())
            return;

        final UserListView self = this;

        if (delegate != null)
            delegate.onLoad(this);

        AlbumDataProxy.getDefault().enqueueCall(
                dataSource.createDataLoadCall(listCountAtOnce, after),
                new Callback<ApiResult>() {
                    @Override
                    public void onResponse(final Response<ApiResult> response, Retrofit retrofit) {
                        if (response.isSuccess() && response.body().isSuccess()) {
                            Application.run(new Runnable() {
                                @Override
                                public void run() {
                                    if (after == null)
                                        items.clear();

                                    lastEntity = (ListEntity) response.body().entity;

                                    items.addAll(lastEntity.data);
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    endDataLoading();
                                    listAdapter.notifyDataSetChanged();

                                    if (delegate != null)
                                        delegate.onLoadComplete(self, lastEntity);
                                }
                            });
                        } else {
                            if (BuildConfig.DEBUG)
                                Log.e("Api Error", response.body());
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

    // ================================================================================================
    //  Interface: DataSource
    // ================================================================================================

    public static interface DataSource<T> {
        boolean followButtonHidden();
        Call<T> createDataLoadCall(int limit, String after);
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public static interface Delegate {
        void onLoad(UserListView listView);
        void onLoadComplete(UserListView listView, ListEntity listEntity);
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
        public BaseUser getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserItemView itemView;

            if (convertView == null) {
                itemView = new UserItemView(context);
                itemView.setAllowsShowFollowButton(!dataSource.followButtonHidden());
                convertView = itemView;
            } else {
                itemView = (UserItemView) convertView;
            }

            itemView.setModel(getItem(position));
            itemView.setSeparatorVisibility(position < items.size() - 1);
            loadMore(position);

            return convertView;
        }
    }
}
