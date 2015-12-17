package com.orcller.app.orcller.fragment;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.AlbumCreateActivity;
import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.activity.PageListActivity;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.event.AlbumEvent;
import com.orcller.app.orcller.itemview.AlbumItemView;
import com.orcller.app.orcller.itemview.TempAlbumItemView;
import com.orcller.app.orcller.manager.MediaUploadUnit;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.AlbumItemViewDelegate;
import com.orcller.app.orcller.proxy.TimelineDataProxy;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.AlbumInfoProfileView;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.event.Event;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSFragment;
import pisces.psuikit.ext.PSListView;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/15/15.
 */
public class TimelineFragment extends PSFragment
        implements AbsListView.OnScrollListener, AlbumItemViewDelegate.Invoker, AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private static final int LIST_COUNT = 20;
    private boolean isCreateButtonAnimating;
    private boolean isDequeueProcessing;
    private int scrollState;
    private Point startPoint;
    private List<Object> items = new ArrayList<>();
    private Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    private ApiUsers.AlbumList lastEntity;
    private AlbumItemViewDelegate albumItemViewDelegate;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListAdapter listAdapter;
    private PSListView listView;
    private FloatingActionButton createButton;
    private AlbumFlipView selectedAlbumFlipView;

    public TimelineFragment() {
        super();
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

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        listView = (PSListView) view.findViewById(R.id.listView);
        createButton = (FloatingActionButton) view.findViewById(R.id.createButton);
        listAdapter = new ListAdapter(getContext());
        albumItemViewDelegate = new AlbumItemViewDelegate(this);

        swipeRefreshLayout.setOnRefreshListener(this);
        listView.setAdapter(listAdapter);
        listView.setItemsCanFocus(true);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        createButton.setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
        ProgressBarManager.hide();

        listAdapter = null;
        albumItemViewDelegate = null;
        listView = null;
        createButton = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        dequeueEvent();
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

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof IndexChangeEvent) {
            IndexChangeEvent casted = (IndexChangeEvent) event;

            if ((casted.getTarget() instanceof PageListActivity) && selectedAlbumFlipView != null)
                selectedAlbumFlipView.setPageIndex(
                        SharedObject.convertPositionToPageIndex(casted.getSelectedIndex()));
        } else if (event instanceof AlbumEvent || event instanceof MediaUploadUnit.Event) {
            eventQueue.offer((Event) event);
            dequeueEvent();
        }
    }

    /**
     * View.OnClickListener
     */
    public void onClick(View v) {
        AlbumCreateActivity.show();
    }

    /**
     * AdapterView.OnItemClickListener
     */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = items.get(position);
        if (item instanceof Album)
            AlbumViewActivity.show(((Album) item).id, false);
    }

    /**
     * AbsListView.OnScrollListener
     */
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;

        if (scrollState == SCROLL_STATE_FLING) {
            startPoint = getScrollPoint();
        } else if (scrollState == SCROLL_STATE_IDLE) {
            startPoint = null;
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (scrollState == SCROLL_STATE_FLING && startPoint != null) {
            int dy = startPoint.y - getScrollPoint().y;
            if (dy > 0) {
                startPoint = null;
                hideCreateButton();
            } else if (dy < 0) {
                startPoint = null;
                showCreateButton();
            }
        }
    }

    /**
     * SwipeRefreshLayout.OnRefreshListener
     */
    public void onRefresh() {
        reload();
    }

    /**
     * AlbumItemViewDelegate.Invoker
     */
    public CommentInputView getCommentInputView() {
        return null;
    }

    public void invalidateOptionsMenu() {
    }

    public void onChangePanningState(boolean isPanning) {
        listView.setScrollable(!isPanning);
        swipeRefreshLayout.setEnabled(!isPanning);
    }

    public void onTap(AlbumFlipView view, FlipView flipView, PageView pageView) {
        selectedAlbumFlipView = view;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void addItem(Object item) {
        if (!items.contains(item)) {
            items.add(0, item);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void deleteItem(Object item) {
        if (items.contains(item)) {
            items.remove(item);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void deleteItem(final MediaUploadUnit unit, Runnable runnable) {
        if (unit == null)
            return;

        Application.run(new Runnable() {
            @Override
            public void run() {
                for (Object item : items) {
                    if (item instanceof Album && ((Album) item).id == unit.getModel().id) {
                        items.remove(item);
                        break;
                    }
                }
            }
        }, runnable);
    }

    private void dequeueEvent() {
        Log.d("dequeueEvent", isDequeueProcessing);
        if (eventQueue.size() < 1)
            return;

        isDequeueProcessing = true;

        final Event event = eventQueue.poll();
        final String type = event.getType();
        final Object target = event.getTarget();
        final Runnable complete = new Runnable() {
            @Override
            public void run() {
                isDequeueProcessing = false;
//                eventQueue.remove(event);
                dequeueEvent();
            }
        };

        Log.d("event.getType()", event.getType());

        if (MediaUploadUnit.Event.START_UPLOADING.equals(type)) {
            final MediaUploadUnit unit = (MediaUploadUnit) target;

            deleteItem(unit, new Runnable() {
                @Override
                public void run() {
                    if (MediaUploadUnit.CompletionState.None.equals(unit.getCompletionState())) {
                        listAdapter.notifyDataSetChanged();
                    } else {
                        addItem(unit);
                    }

                    complete.run();
                }
            });
        } else if (target instanceof MediaUploadUnit &&
                (AlbumEvent.CREATE.equals(type) || AlbumEvent.MODIFY.equals(type))) {
            deleteItem(target);
            addItem(((MediaUploadUnit) target).getModel());
            complete.run();
        } else if (AlbumEvent.DELETE.equals(type)) {
            deleteItem(event.getObject());
            complete.run();
        }
    }

    private Point getScrollPoint() {
        View view = listView.getChildAt(0);
        return view != null ? new Point(view.getLeft(), view.getTop()) : null;
    }

    private void hideCreateButton() {
        if (!createButton.isShown() || isCreateButtonAnimating)
            return;

        createButton
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .y(listView.getHeight())
                .alpha(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isCreateButtonAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        createButton.setVisibility(View.GONE);
                        isCreateButtonAnimating = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                })
                .start();
    }

    private void load(final String after) {
        if (invalidDataLoading())
            return;

        if (isFirstLoading())
            ProgressBarManager.show();

        TimelineDataProxy.getDefault().list(LIST_COUNT, after, new Callback<ApiUsers.AlbumListRes>() {
            @Override
            public void onResponse(final Response<ApiUsers.AlbumListRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    if (after == null)
                        items.clear();

                    lastEntity = response.body().entity;

                    items.addAll(lastEntity.data);

                    AlbumDataProxy.getDefault().view(4, new Callback<ApiAlbum.AlbumRes>() {
                        @Override
                        public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                            MediaUploadUnit unit = new MediaUploadUnit(response.body().entity);
                            items.add(0, unit);
                            endDataLoading();
                            listAdapter.notifyDataSetChanged();
                            listView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFailure(Throwable t) {
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

    private void reload() {
        load(null);
    }

    private void showCreateButton() {
        if (createButton.isShown() || isCreateButtonAnimating)
            return;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) createButton.getLayoutParams();
        createButton.setVisibility(View.VISIBLE);
        createButton
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .y(listView.getHeight() - createButton.getHeight() - params.bottomMargin)
                .alpha(1)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isCreateButtonAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isCreateButtonAnimating = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    // ================================================================================================
    //  Class: ListAdapter
    // ================================================================================================

    private class ListAdapter extends BaseAdapter {
        private static final int REAL = 1;
        private static final int TEMP = 2;
        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return items != null ? items.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position) instanceof Album ? REAL : TEMP;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object item = getItem(position);
            int type = getItemViewType(position);

            switch (type) {
                case REAL:
                    AlbumItemView albumItemView;

                    if (convertView == null || !(convertView instanceof AlbumItemView)) {
                        albumItemView = new AlbumItemView(context);
                        albumItemView.setAllowsShowOptionIcon(true);
                        albumItemView.setDescriptionMode(AlbumInfoProfileView.ALBUM_NAME);
                        albumItemView.setDelegate(albumItemViewDelegate);
                        convertView = albumItemView;
                    } else {
                        albumItemView = (AlbumItemView) convertView;
                    }

                    albumItemView.setModel((Album) item);
                    break;

                case TEMP:
                    TempAlbumItemView tempAlbumItemView;

                    if (convertView == null || !(convertView instanceof TempAlbumItemView)) {
                        tempAlbumItemView = new TempAlbumItemView(context);
                        convertView = tempAlbumItemView;
                    } else {
                        tempAlbumItemView = (TempAlbumItemView) convertView;
                    }

                    tempAlbumItemView.setUnit((MediaUploadUnit) item);
                    break;
            }

            loadMore(position);

            return convertView;
        }

        @Override
        public int getViewTypeCount(){
            return 2;
        }
    }
}
