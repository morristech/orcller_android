package com.orcller.app.orcller.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.UserItemView;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.ListEntity;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.UserDataGridView;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.model.BaseUser;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSFragment;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/15/15.
 */
public class TimelineFragment extends PSFragment {
    private List<Album> items = new ArrayList<>();
    private ListView listView;
    private ListEntity lastEntity;
    private ListAdapter listAdapter;

    public TimelineFragment() {
    }

    // ================================================================================================
    //  Overridden: PSFragment
    // ================================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(final String after) {
        if (invalidDataLoading())
            return;

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
                                }
                            });
                        } else {
                            if (DEBUG)
                                e("Api Error", response.body());
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
