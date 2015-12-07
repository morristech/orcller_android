package com.orcller.app.orcller.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.CommentItemView;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Comment;
import com.orcller.app.orcller.model.album.Comments;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSListView;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.PSButton;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/5/15.
 */
public class CommentListView extends PSListView
        implements CommentItemView.Delegate, View.OnClickListener {
    public enum SortDirection {
        Bottom, Top
    }

    private final int LOAD_LIMIT_COUNT = 5;
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

        items.addAll(comments.data);
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
        if (dataLoadValidator.invalidDataLoading())
            return;

        ProgressBarManager.show();

        final Comment comment = target.getModel();
        final Runnable error = new Runnable() {
            @Override
            public void run() {
                dataLoadValidator.endDataLoading();
                ProgressBarManager.hide();

                AlertDialogUtils.show(getResources().getString(R.string.m_message_fail),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == AlertDialog.BUTTON_POSITIVE) {
                                    onClickDeleteButton(target);
                                }
                            }
                        },
                        getResources().getString(R.string.w_dismiss),
                        getResources().getString(R.string.w_retry)
                );
            }
        };

        AlbumDataProxy.getDefault().enqueueCall(
                createUncommentCall(comment),
                new Callback<ApiAlbum.CommentsRes>() {
                    @Override
                    public void onResponse(Response<ApiAlbum.CommentsRes> response, Retrofit retrofit) {
                        if (response.isSuccess() && response.body().isSuccess()) {
                            lastEntity = response.body().entity;
                            items.remove(comment);
                            listAdapter.notifyDataSetChanged();
                            dataLoadValidator.endDataLoading();
                            ProgressBarManager.hide();
                            performChange();
                        } else {
                            error.run();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
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
        if (dataLoadValidator.invalidDataLoading())
            return;

        AlbumDataProxy.getDefault().enqueueCall(
                createCommentsCall(LOAD_LIMIT_COUNT, after),
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
                                    dataLoadValidator.endDataLoading();
                                    listAdapter.notifyDataSetChanged();
                                    performChange();
                                    setHeaderView();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        dataLoadValidator.endDataLoading();
                    }
                });
    }

    private void loadMore(int position) {
        if (sortDirection.equals(SortDirection.Top) &&
                lastEntity != null &&
                items.size() < lastEntity.total_count &&
                position >= items.size() - 3)
            loadAfter();
    }

    private void loadAfter() {
        if (lastEntity != null && lastEntity.after != null)
            load(lastEntity.after);
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

    public static interface Delegate {
        void onChange(Comments comments);
    }
}
