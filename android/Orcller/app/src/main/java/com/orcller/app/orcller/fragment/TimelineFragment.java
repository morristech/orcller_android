package com.orcller.app.orcller.fragment;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.AlbumCreateActivity;
import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.activity.PageListActivity;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.event.AlbumEvent;
import com.orcller.app.orcller.event.RelationshipsEvent;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.AlbumItemView;
import com.orcller.app.orcller.itemview.LoadMoreFooterView;
import com.orcller.app.orcller.itemview.TempAlbumItemView;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.manager.MediaUploadUnit;
import com.orcller.app.orcller.manager.ModelFileCacheManager;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.AlbumItemViewDelegate;
import com.orcller.app.orcller.proxy.TimelineDataProxy;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.AlbumInfoProfileView;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.event.Event;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSListView;
import pisces.psuikit.widget.ExceptionView;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/15/15.
 */
public class TimelineFragment extends MainTabFragment
        implements AbsListView.OnScrollListener, AlbumItemViewDelegate.Invoker, AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private static final int LIST_COUNT = 10;
    private boolean isCreateButtonAnimating;
    private boolean shouldReload = true;
    private int scrollState;
    private Point startPoint;
    private List<Object> items = new ArrayList<>();
    private Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    private Error loadError;
    private ApiUsers.AlbumList lastEntity;
    private AlbumItemViewDelegate albumItemViewDelegate;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FrameLayout container;
    private Button newPostButton;
    private ListAdapter listAdapter;
    private View listHeaderView;
    private LoadMoreFooterView listFooterView;
    private PSListView listView;
    private FloatingActionButton createButton;
    private AlbumFlipView selectedAlbumFlipView;

    public TimelineFragment() {
        super();
    }

    // ================================================================================================
    //  Overridden: MainTabFragment
    // ================================================================================================

    @Override
    public void onResume() {
        super.onResume();

        dequeueEvent();
    }

    @Override
    public void onPause() {
        super.onPause();

        albumItemViewDelegate.pauseAlbumFlipView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, null);
    }

    @Override
    protected void setUpSubviews(View view) {
        super.setUpSubviews(view);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        container = (FrameLayout) view.findViewById(R.id.container);
        listView = (PSListView) view.findViewById(R.id.listView);
        createButton = (FloatingActionButton) view.findViewById(R.id.createButton);
        newPostButton = (Button) view.findViewById(R.id.newPostButton);
        listAdapter = new ListAdapter(getContext());
        albumItemViewDelegate = new AlbumItemViewDelegate(this);
        listHeaderView = new View(getContext());
        listFooterView = new LoadMoreFooterView(getContext());

        listHeaderView.setBackgroundColor(getResources().getColor(R.color.theme_white_accent));
        listHeaderView.setLayoutParams(new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, GraphicUtils.convertDpToPixel(5)));
        listView.addHeaderView(listHeaderView);
        exceptionViewManager.add(
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NoTimeline, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.NetworkError, container),
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, container));
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_purple_accent);
        swipeRefreshLayout.setOnRefreshListener(this);
        listView.setItemsCanFocus(true);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        createButton.setOnClickListener(this);
        newPostButton.setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(null);
        listView.setOnItemClickListener(null);
        listView.setOnScrollListener(null);
        createButton.setOnClickListener(null);
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

        newPostButton.setVisibility(View.GONE);
        listView.removeFooterView(listFooterView);
        exceptionViewManager.validate();
    }

    @Override
    public String getToolbarTitle() {
        return null;
    }

    @Override
    public void onClick(ExceptionView view) {
        if (ExceptionViewFactory.Type.NoTimeline.equals(view.getTag())) {
            AlbumCreateActivity.show();
        } else {
            exceptionViewManager.clear();
            reload();
        }
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.NoTimeline.equals(view.getTag()))
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

    @Override
    protected void startFragment() {
        if (listView.getAdapter() == null)
            listView.setAdapter(listAdapter);

        invalidateLoading();
    }

    @Override
    public void scrollToTop() {
        if (listView != null)
            listView.setSelection(0);
    }

    // ================================================================================================
    //  Interface Implementation
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
        } else if (event instanceof AlbumEvent) {
            AlbumEvent casted = (AlbumEvent) event;

            if (!AlbumEvent.DELETE.equals(casted.getType())) {
                Album album = (Album) casted.getObject();
                lastEntity.time = album.updated_time;
                TimelineDataProxy.getDefault().setLastViewDate(lastEntity.time);
            }

            if (!AlbumEvent.PREPARE.equals(casted.getType())) {
                eventQueue.offer((Event) event);
                dequeueEvent();
            }
        } else if (event instanceof MediaUploadUnit.Event) {
            MediaUploadUnit.Event casted = (MediaUploadUnit.Event) event;

            if (MediaUploadUnit.Event.START_UPLOADING.equals(casted.getType())) {
                eventQueue.offer((Event) event);
                dequeueEvent();
            }
        } else if (event instanceof SharedObject.Event &&
                SharedObject.Event.CHANGE_NEWS_COUNT.equals(((SharedObject.Event) event).getType())) {

            if (SharedObject.get().getTimelineCount() > 0)
                newPostButton.setVisibility(View.VISIBLE);
        } else if (event instanceof RelationshipsEvent) {
            RelationshipsEvent casted = (RelationshipsEvent) event;

            if (RelationshipsEvent.FOLLOW.equals(casted.getType()) ||
                    RelationshipsEvent.UNFOLLOW.equals(casted.getType())) {
                reload();
            }
        }
    }

    /**
     * View.OnClickListener
     */
    public void onClick(View v) {
        if (createButton.equals(v)) {
            AlbumCreateActivity.show();
        } else if (newPostButton.equals(v)) {
            shouldReload = true;

            listView.setSelection(0);
            reload();
        }
    }

    /**
     * AdapterView.OnItemClickListener
     */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = items.get(position - 1);
        if (item instanceof Album) {
            AlbumViewActivity.show(((Album) item).id, false);
        }
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
        if (scrollState != SCROLL_STATE_IDLE)
            albumItemViewDelegate.pauseAlbumFlipView();

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

    public void onAlbumInfoSynchronize(AlbumItemView itemView, AlbumAdditionalListEntity model) {
        if (items.get(0).equals(itemView.getModel())) {
            lastEntity.time = itemView.getModel().updated_time;
            cacheItems();
        }
    }

    public void onAlbumSynchronize(AlbumItemView itemView) {
        if (items.get(0).equals(itemView.getModel())) {
            lastEntity.time = itemView.getModel().updated_time;
            cacheItems();
        }
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
            scrollToTop();
        }
    }

    private void cacheItems() {
        if (lastEntity != null) {
            TimelineDataProxy.getDefault().setLastViewDate(lastEntity.time);
            ModelFileCacheManager.save(ModelFileCacheManager.Type.Timeline, items);
        }
    }

    private void deleteAlbum(long albumId) {
        Album target = null;

        for (Object item : items) {
            if (item instanceof Album && ((Album) item).id == albumId) {
                target = (Album) item;
                break;
            }
        }

        if (target != null) {
            items.remove(target);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void deleteUnit(MediaUploadUnit unit) {
        if (items.contains(unit)) {
            items.remove(unit);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void dequeueEvent() {
        if (!isActive() || eventQueue.size() < 1)
            return;

        final Event event = eventQueue.poll();
        final String type = event.getType();
        final Object target = event.getTarget();

        if (MediaUploadUnit.Event.START_UPLOADING.equals(type)) {
            final MediaUploadUnit unit = (MediaUploadUnit) target;

            if (unit.isCancelled()) {
                dequeueEvent();
            } else {
                deleteAlbum(unit.getModel().id);

                if (MediaUploadUnit.CompletionState.None.equals(unit.getCompletionState())) {
                    listAdapter.notifyDataSetChanged();
                } else {
                    addItem(unit);
                }

                dequeueEvent();
                exceptionViewManager.validate();
                cacheItems();
            }
        } else if (target instanceof MediaUploadUnit &&
                (AlbumEvent.CREATE.equals(type) || AlbumEvent.MODIFY.equals(type))) {
            deleteUnit((MediaUploadUnit) target);
            addItem(event.getObject());
            exceptionViewManager.validate();
            cacheItems();
            dequeueEvent();
        } else if (AlbumEvent.DELETE.equals(type)) {
            Album album = (Album) event.getObject();
            deleteAlbum(album.id);
            exceptionViewManager.validate();
            cacheItems();
            dequeueEvent();
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

    private boolean invalidDataLoading(String after) {
        boolean invalid = invalidDataLoading();

        if (invalid)
            return invalid;

        if (isFirstLoading()) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        } else if (after != null) {
            listView.addFooterView(listFooterView);
        }

        return invalid;
    }

    private void invalidateLoading() {
        if (isActive() && shouldReload) {
            newPostButton.setVisibility(View.GONE);
            load(null);
        }
    }

    private void load(final String after) {
        if (invalidDataLoading(after))
            return;

        loadError = null;

        TimelineDataProxy.getDefault().list(LIST_COUNT, after, new Callback<ApiUsers.AlbumListRes>() {
            @Override
            public void onResponse(final Response<ApiUsers.AlbumListRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    Application.run(new Runnable() {
                        @Override
                        public void run() {
                            lastEntity = response.body().entity;

                            if (after == null) {
                                items.clear();
                                items.addAll(MediaManager.getDefault().getCachedUploadUnits());
                                AlbumDataProxy.getDefault().clearCacheAfterCompare(lastEntity);
                                TimelineDataProxy.getDefault().setLastViewDate(lastEntity.time);
                                SharedObject.get().setTimelineCount(0);
                            }

                            items.addAll(lastEntity.data);
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            endDataLoading();
                            listAdapter.notifyDataSetChanged();
                            cacheItems();

                            if (after == null)
                                MediaManager.getDefault().continueUploading();

                            shouldReload = false;
                        }
                    });
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());

                    items = items.size() < 1 ? ModelFileCacheManager.load(ModelFileCacheManager.Type.Timeline, items) : items;
                    loadError = items.size() < 1 ? new APIError(response.body()) : null;

                    if (loadError == null)
                        listAdapter.notifyDataSetChanged();

                    endDataLoading();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                items = items.size() < 1 ? ModelFileCacheManager.load(ModelFileCacheManager.Type.Timeline, items) : items;
                loadError = items.size() < 1 ? new Error(t.getMessage()) : null;

                if (loadError == null)
                    listAdapter.notifyDataSetChanged();

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
        shouldReload = true;

        invalidateLoading();
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
        private static final int NONE = -1;
        private static final int REAL = 0;
        private static final int TEMP = 1;
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
            if (items.get(position) instanceof Album)
                return REAL;
            if (items.get(position) instanceof MediaUploadUnit)
                return TEMP;
            return NONE;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                int type = getItemViewType(position);
                switch (type) {
                    case REAL:
                        AlbumItemView albumItemView = new AlbumItemView(context);
                        albumItemView.setAllowsShowOptionIcon(true);
                        albumItemView.setDescriptionMode(AlbumInfoProfileView.ALBUM_NAME);
                        albumItemView.setDelegate(albumItemViewDelegate);
                        convertView = albumItemView;
                        break;

                    case TEMP:
                        TempAlbumItemView tempAlbumItemView = new TempAlbumItemView(context);
                        tempAlbumItemView.setDescriptionMode(AlbumInfoProfileView.ALBUM_NAME);
                        convertView = tempAlbumItemView;
                        break;
                }
            }

            Object item = getItem(position);

            if (convertView instanceof AlbumItemView) {
                ((AlbumItemView) convertView).setModel((Album) item);
            } else if (convertView instanceof TempAlbumItemView) {
                ((TempAlbumItemView) convertView).setUnit((MediaUploadUnit) item);
            }

            loadMore(position);

            return convertView;
        }
    }
}
