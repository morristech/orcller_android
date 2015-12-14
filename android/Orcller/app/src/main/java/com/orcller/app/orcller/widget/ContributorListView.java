package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.ContributorItemView;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Comment;
import com.orcller.app.orcller.model.album.Contributor;
import com.orcller.app.orcller.model.album.Contributors;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.model.api.ApiCoedit;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.CoeditDataProxy;

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

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/14/15.
 */
public class ContributorListView extends PSListView implements CoeditButton.Delegate {
    public static final int CONTRIBUTORS = 1;
    public static final int STANDBY = 2;
    private int dataType;
    private List<Contributor> accepts = new ArrayList<>();
    private List<Contributor> asks = new ArrayList<>();
    private List<Contributor> invites = new ArrayList<>();
    private List<Contributor> others = new ArrayList<>();
    private Album model;
    private Contributors lastEntity;
    private ListAdapter listAdapter;

    public ContributorListView(Context context) {
        super(context);
    }

    public ContributorListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContributorListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSListView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        listAdapter = new ListAdapter(context, this);

        setDivider(null);
        setAdapter(listAdapter);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public Album getModel() {
        return model;
    }

    public void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        reload();
    }

    public void reload() {
        load();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * CoeditButton.Delegate
     */
    public void onChange(CoeditButton target, Contributors contributors) {
        processingItems();
    }

    public void onSync(CoeditButton target, Contributors contributors) {
        processingItems();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected Call<ApiAlbum.CommentsRes> createCommentsCall(int limit, String prev) {
        return AlbumDataProxy.getDefault().service().comments(model.id, limit, prev);
    }

    protected Call<ApiAlbum.CommentsRes> createUncommentCall(Comment comment) {
        return AlbumDataProxy.getDefault().service().uncomment(model.id, comment.id);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private List<Contributor> getItems(int status) {
        if (status == Contributor.Status.Accept.value())
            return accepts;
        if (status == Contributor.Status.Ask.value())
            return asks;
        if (status == Contributor.Status.Invite.value())
            return invites;
        return others;
    }

    private void load() {
        if (invalidDataLoading())
            return;

        if (dataType == STANDBY) {
            call(CoeditDataProxy.getDefault().service().standby(model.id));
        } else if (dataType == CONTRIBUTORS) {
            call(CoeditDataProxy.getDefault().service().contributors(model.id));
        }
    }

    private void call(Call<ApiCoedit.ContributorsRes> call) {
        CoeditDataProxy.getDefault().enqueueCall(call, new Callback<ApiCoedit.ContributorsRes>() {
            @Override
            public void onResponse(final Response<ApiCoedit.ContributorsRes> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    lastEntity = response.body().entity;

                    if (dataType == STANDBY)
                        processingItems();
                    else
                        listAdapter.notifyDataSetChanged();
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

    private void processingItems() {
        Application.run(new Runnable() {
            @Override
            public void run() {
                accepts.clear();
                asks.clear();
                invites.clear();
                others.clear();

                for (Contributor contributor : lastEntity.data) {
                    getItems(contributor.contributor_status).add(contributor);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    // ================================================================================================
    //  Class: ListAdapter
    // ================================================================================================

    private class ListAdapter extends BaseAdapter {
        private Context context;
        private CoeditButton.Delegate delegate;

        public ListAdapter(Context context, CoeditButton.Delegate delegate) {
            this.context = context;
            this.delegate = delegate;
        }

        // ================================================================================================
        //  Overridden: BaseAdapter
        // ================================================================================================

        @Override
        public int getCount() {
            return lastEntity != null ? lastEntity.data.size() : 0;
        }

        @Override
        public Contributor getItem(int position) {
            if (dataType == CONTRIBUTORS)
                return lastEntity.data.get(position);

            if (position < accepts.size())
                return accepts.get(position);
            if (position < accepts.size() + asks.size())
                return asks.get(position - accepts.size());
            if (position < invites.size() + accepts.size() + asks.size())
                return invites.get(position - accepts.size() - asks.size());
            return others.get(position - accepts.size() - asks.size() - invites.size());
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContributorItemView itemView;

            if (convertView == null) {
                itemView = new ContributorItemView(context);
                itemView.setDelegate(delegate);
                convertView = itemView;
            } else {
                itemView = (ContributorItemView) convertView;
            }

            setItemView(itemView, getItem(position));

            return convertView;
        }

        // ================================================================================================
        //  Private
        // ================================================================================================

        private boolean isHiddenSeperator(List<Contributor> list, Contributor contributor) {
            return list != null && !list.isEmpty() && ObjectUtils.equals(contributor, list.get(list.size() - 1));
        }

        private boolean isShownHeader(List<Contributor> list, Contributor contributor) {
            return list != null && !list.isEmpty() && ObjectUtils.equals(contributor, list.get(0));
        }

        private void setItemView(ContributorItemView itemView, Contributor contributor) {
            if (dataType == STANDBY) {
                setHeaderText(itemView, contributor);
                itemView.setSeparatorVisibility(
                        !isHiddenSeperator(accepts, contributor)
                                && !isHiddenSeperator(asks, contributor)
                                && !isHiddenSeperator(invites, contributor)
                                && !isHiddenSeperator(others, contributor));
            } else if (lastEntity != null) {
                if (isShownHeader(lastEntity.data, contributor))
                    itemView.setHeaderText(R.string.w_collaborators);
                else
                    itemView.setHeaderText(null);

                itemView.setSeparatorVisibility(!isHiddenSeperator(lastEntity.data, contributor));
            }

            itemView.setModel(contributor, model.id);
        }

        private void setHeaderText(ContributorItemView itemView, Contributor contributor) {
            if (isShownHeader(accepts, contributor))
                itemView.setHeaderText(R.string.w_collaborators);
            else if (isShownHeader(asks, contributor))
                itemView.setHeaderText(R.string.w_requests);
            else if (isShownHeader(invites, contributor))
                itemView.setHeaderText(R.string.w_invites);
            else if (isShownHeader(others, contributor))
                itemView.setHeaderText(R.string.w_add_collaborators);
            else
                itemView.setHeaderText(null);
        }
    }
}
