package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.CommentItemView;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Comment;
import com.orcller.app.orcller.model.Comments;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSListView;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.PSButton;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/5/15.
 */
public class CommentListView extends PSListView
        implements CommentItemView.Delegate, View.OnClickListener {
    public enum SortDirection {
        Bottom, Top
    }

    private int listCountAtOnce;
    private List<Comment> items = new ArrayList<>();
    private SortDirection sortDirection = SortDirection.Bottom;
    private Comments lastEntity;
    private Album model;
    private Delegate delegate;
    private ListAdapter listAdapter;
    private PSButton headerView;

    public CommentListView(Context context) {
        super(context);
    }

    public CommentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSListView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        listAdapter = new ListAdapter(context, this);
        headerView = (PSButton) inflate(context, R.layout.itemview_comment_header, null);

        headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, GraphicUtils.convertDpToPixel(40)));
        headerView.setOnClickListener(this);
        setDivider(null);
        setAdapter(listAdapter);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CommentListView, defStyleAttr, defStyleRes);
        try {
            setListCountAtOnce(ta.getInt(R.styleable.CommentListView_listCountAtOnce, 5));
        } finally {
            ta.recycle();
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void setListCountAtOnce(int listCountAtOnce) {
        this.listCountAtOnce = listCountAtOnce;
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public Album getModel() {
        return model;
    }

    public void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        refresh();
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public void add(Comments comments) {
        if (lastEntity != null)
            lastEntity.total_count = comments.total_count;

        if (sortDirection.equals(SortDirection.Bottom)) {
            items.addAll(comments.data);
        } else if (sortDirection.equals(SortDirection.Top)) {
            items.addAll(0, comments.data);
        }

        listAdapter.notifyDataSetChanged();
    }

    public void clear() {
        lastEntity = null;

        items.clear();
        listAdapter.notifyDataSetChanged();
        setHeaderView();
    }

    public void refresh() {
        load(null);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onClick(View v) {
        loadAfter();
    }

    public void onClickDeleteButton(final CommentItemView target) {
        if (invalidDataLoading())
            return;

        ProgressBarManager.show();

        final Comment comment = target.getModel();
        final Runnable error = new Runnable() {
            @Override
            public void run() {
                endDataLoading();
                ProgressBarManager.hide();

                AlertDialogUtils.retry(R.string.m_fail_common, new Runnable() {
                    @Override
                    public void run() {
                        onClickDeleteButton(target);
                    }
                });
            }
        };

        AlbumDataProxy.getDefault().enqueueCall(
                createUncommentCall(comment),
                new Callback<ApiAlbum.CommentsRes>() {
                    @Override
                    public void onResponse(Response<ApiAlbum.CommentsRes> response, Retrofit retrofit) {
                        if (response.isSuccess() && response.body().isSuccess()) {
                            Comments entity = response.body().entity;
                            lastEntity.total_count = entity.total_count;
                            lastEntity.participated = entity.participated;

                            lastEntity.didChangeProperties();
                            items.remove(comment);
                            listAdapter.notifyDataSetChanged();
                            endDataLoading();
                            ProgressBarManager.hide();
                            performChange();
                        } else {
                            if (DEBUG)
                                e("Api Error", response.body());

                            error.run();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (BuildConfig.DEBUG)
                            Log.e("onFailure", t);

                        error.run();
                    }
                });
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

    private void load(final String after) {
        if (invalidDataLoading())
            return;

        if (delegate != null)
            delegate.onLoad(this);

        final CommentListView target = this;

        AlbumDataProxy.getDefault().enqueueCall(
                createCommentsCall(listCountAtOnce, after),
                new Callback<ApiAlbum.CommentsRes>() {
                    @Override
                    public void onResponse(final Response<ApiAlbum.CommentsRes> response, Retrofit retrofit) {
                        if (response.isSuccess() && response.body().isSuccess()) {
                            Application.run(new Runnable() {
                                @Override
                                public void run() {
                                    if (after == null)
                                        items.clear();

                                    lastEntity = response.body().entity;

                                    if (sortDirection.equals(SortDirection.Bottom)) {
                                        lastEntity.sort(true);
                                        items.addAll(0, lastEntity.data);
                                    } else if (sortDirection.equals(SortDirection.Top)) {
                                        items.addAll(lastEntity.data);
                                    }
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    endDataLoading();

                                    if (delegate != null)
                                        delegate.onLoadComplete(target);

                                    listAdapter.notifyDataSetChanged();
                                    performChange();
                                    setHeaderView();
                                }
                            });
                        } else {
                            if (BuildConfig.DEBUG)
                                Log.e("Api Error", response.body());

                            endDataLoading();

                            if (delegate != null)
                                delegate.onFailure(target, new APIError(response.body()));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (BuildConfig.DEBUG)
                            Log.e("onFailure", t);

                        endDataLoading();

                        if (delegate != null)
                            delegate.onFailure(target, new Error(t.getMessage()));
                    }
                });
    }

    private void loadAfter() {
        if (lastEntity != null && lastEntity.after != null)
            load(lastEntity.after);
    }

    private void loadMore(int position) {
        if (sortDirection.equals(SortDirection.Top) &&
                lastEntity != null &&
                items.size() < lastEntity.total_count &&
                position >= items.size() - 3)
            loadAfter();
    }

    private void performChange() {
        if (getDelegate() != null)
            getDelegate().onChange(lastEntity);
    }

    private void setHeaderView() {
        if (sortDirection.equals(SortDirection.Bottom) &&
                lastEntity != null &&
                items.size() < lastEntity.total_count) {
            addHeaderView(headerView);
        } else {
            removeHeaderView(headerView);
        }
    }

    // ================================================================================================
    //  Class: ListAdapter
    // ================================================================================================

    private class ListAdapter extends BaseAdapter {
        private Context context;
        private CommentItemView.Delegate delegate;

        public ListAdapter(Context context, CommentItemView.Delegate delegate) {
            this.context = context;
            this.delegate = delegate;
        }

        @Override
        public int getCount() {
            return items != null ? items.size() : 0;
        }

        @Override
        public Comment getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommentItemView itemView;

            if (convertView == null) {
                itemView = new CommentItemView(context);
                convertView = itemView;

                itemView.setDelegate(delegate);
            } else {
                itemView = (CommentItemView) convertView;
            }

            itemView.setModel(getItem(position));
            itemView.setSeparatorVisibility(position < items.size() - 1);
            loadMore(position);

            return convertView;
        }
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public interface Delegate {
        void onChange(Comments comments);
        void onFailure(CommentListView listView, Error error);
        void onLoad(CommentListView listView);
        void onLoadComplete(CommentListView listView);
    }
}
