package com.orcller.app.orcller.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.album.Page;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.MediaView;
import com.orcller.app.orcller.widget.PageScrollView;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSRecyclerViewPager;
import pisces.psuikit.imagepicker.OnScrollListener;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/3/15.
 */
public class PageListActivity extends PSActionBarActivity
        implements RecyclerViewPager.OnPageChangedListener, CommentInputView.Delegate, PageScrollView.Delegate, ViewTreeObserver.OnGlobalLayoutListener {
    public static final String ALBUM_KEY = "album";
    public static final String SELECTED_INDEX_KEY = "selectedIndex";
    private int selectedIndex = -1;
    private ArrayList<Media> mediaList = new ArrayList<>();
    private Album model;
    private LinearLayout rootLayout;
    private TextView toolbarTextView;
    private PSRecyclerViewPager recyclerView;
    private PageScrollView selectedView;
    private CommentInputView commentInputView;
    private Adapter adapter;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pagelist);

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        toolbarTextView = (TextView) findViewById(R.id.toolbarTextView);
        recyclerView = (PSRecyclerViewPager) findViewById(R.id.recyclerView);
        commentInputView = (CommentInputView) findViewById(R.id.commentInputView);
        adapter = new Adapter(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle(getString(R.string.w_title_edit_page));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnPageChangedListener(this);
        recyclerView.addOnScrollListener(new OnScrollListener());
        commentInputView.setDelegate(this);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        SoftKeyboardNotifier.getDefault().register(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pagelist, menu);
        return true;
    }

    @Override
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        setRecyclerViewLayout();
        setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        SoftKeyboardNotifier.getDefault().unregister(this);
        ProgressBarManager.hide(this);
        stopSelectedVideoMediaView();
        recyclerView.removeOnPageChangedListener(this);
        recyclerView.removeAllViews();

        model = null;
        adapter = null;
        rootLayout = null;
        toolbarTextView = null;
        recyclerView = null;
        commentInputView = null;
        selectedView = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (selectedView.isEditEnabled()) {
                    setEditEnabled();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                if (selectedView.isEditEnabled()) {
                    updatePage();
                } else {
                    setEditEnabled();
                }
                break;

            case android.R.id.home:
                if (selectedView.isEditEnabled()) {
                    setEditEnabled();
                } else {
                    onBackPressed();
                }
                break;

            default:
                break;
        }
        return true;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album model, int selectedIndex) {
        Intent intent = new Intent(Application.applicationContext(), PageListActivity.class);
        intent.putExtra(ALBUM_KEY, model);
        intent.putExtra(SELECTED_INDEX_KEY, selectedIndex);
        Application.startActivity(intent, R.animator.fadein, R.animator.fadeout);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onClickCommentButton(PageScrollView target) {
        commentInputView.setFocus();
    }

    public void onClickHeartButton(PageScrollView target) {
        if (invalidDataLoading())
            return;

        final Page page = selectedView.getModel();

        AlbumDataProxy.getDefault().likeOfPage(page, new Callback<ApiAlbum.LikesRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.LikesRes> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    page.likes.synchronize(response.body().entity);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                endDataLoading();
            }
        });

        page.likes.participated();
        selectedView.updateButtons();
    }

    public void onClickMediaView(PageScrollView target, MediaView mediaView) {
        MediaListActivity.show(mediaList, selectedIndex);
    }

    public void onClickPostButton() {
        if (invalidDataLoading())
            return;

        commentInputView.clearFocus();
        ProgressBarManager.show(this);

        String message = commentInputView.getText().toString().trim();
        final Page page = selectedView.getModel();
        final Runnable retry = new Runnable() {
            @Override
            public void run() {
                onClickPostButton();
            }
        };

        AlbumDataProxy.getDefault().commentOfPage(page.id, message, new Callback<ApiAlbum.CommentsRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.CommentsRes> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    commentInputView.clear();
                    page.comments.synchronize(response.body().entity);
                    selectedView.addComments(response.body().entity);
                } else {
                    AlertDialogUtils.retry(R.string.m_fail_comment, retry);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                endDataLoading();
                AlertDialogUtils.retry(R.string.m_fail_comment, retry);
            }
        });
    }

    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                if (selectedView.isEditEnabled()) {
                    selectedView.scrollTo(0, (int) casted.getObject());
                } else {
                    recyclerView.setScrollable(false);
                }
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                if (selectedView.isEditEnabled()) {
                    selectedView.scrollTo(0, 0);
                } else {
                    recyclerView.setScrollable(true);
                }
            }
        } else if (event instanceof IndexChangeEvent) {
            IndexChangeEvent casted = (IndexChangeEvent) event;

            if (casted.getTarget() instanceof MediaListActivity)
                setSelectedIndex(casted.getSelectedIndex());
        }
    }

    public void OnPageChanged(int oldPosition, int newPosition) {
        boolean initialSelection = newPosition == selectedIndex;

        setSelectedIndex(newPosition);

        if (selectedView == null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedIndex);

            if (viewHolder != null)
                selectedView = (PageScrollView) viewHolder.itemView;

            if (!initialSelection)
                EventBus.getDefault().post(new IndexChangeEvent(
                        IndexChangeEvent.INDEX_CHANGE, this, selectedView, selectedIndex));
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setModel(final Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                mediaList.clear();

                for (Page page : model.pages.data) {
                    mediaList.add(page.media);
                }
            }
        });

        modelChanged();
    }

    private void setSelectedIndex(int selectedIndex) {
        if (selectedIndex == this.selectedIndex)
            return;

        this.selectedIndex = selectedIndex;

        if (selectedView != null) {
            if (selectedView.hasImageMediaScrollView())
                selectedView.getImageMediaScrollView().reset();

            selectedView.setEnabled(false);
            stopSelectedVideoMediaView();

            selectedView = null;
        }

        recyclerView.scrollToPosition(selectedIndex);
        updateTitle();
    }

    private void modelChanged() {
        adapter.setItems(model.pages.data);
        setSelectedIndex(getIntent().getIntExtra(SELECTED_INDEX_KEY, 0));
    }

    private void requestUpdatePage(final Page page) {
        if (invalidDataLoading())
            return;

        ProgressBarManager.show(this, true);

        final Runnable error = new Runnable() {
            @Override
            public void run() {
                AlertDialogUtils.show(getString(R.string.m_fail_common),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == AlertDialog.BUTTON_POSITIVE) {
                                    requestUpdatePage(page);
                                } else {
                                    setEditEnabled();
                                }
                            }
                        },
                        getString(R.string.w_dismiss),
                        getString(R.string.w_retry)
                );
            }
        };

        AlbumDataProxy.getDefault().updatePage(page, new Callback<ApiResult>() {
            @Override
            public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    selectedView.getModel().synchronize(page);
                    setEditEnabled();
                } else {
                    error.run();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                endDataLoading();
                error.run();
            }
        });
    }

    private void setEditEnabled() {
        if (selectedView == null)
            return;

        selectedView.setEditEnabled(!selectedView.isEditEnabled());
        selectedView.reloadDescription();
        recyclerView.setScrollable(!selectedView.isEditEnabled());
        commentInputView.setVisibility(selectedView.isEditEnabled() ? View.GONE : View.VISIBLE);
        commentInputView.clearFocus();
        setRecyclerViewLayout();
        updateTitle();

        Drawable icon = selectedView.isEditEnabled() ? null : getDrawable(R.drawable.icon_page_edit);
        String title = selectedView.isEditEnabled() ? getString(R.string.w_done) : null;
        MenuItem item = getToolbar().getMenu().findItem(R.id.edit);
        item.setIcon(icon);
        item.setTitle(title);
    }

    private void setRecyclerViewLayout() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
        params.bottomMargin = commentInputView.isShown() ? commentInputView.getHeight() : 0;
    }

    private void stopSelectedVideoMediaView() {
        if (selectedView != null) {
            if (selectedView.getVideoMediaView() != null)
                selectedView.getVideoMediaView().stop();
        }
    }

    private void updatePage() {
        try {
            final Page clonedPage = (Page) selectedView.getModel().clone();
            clonedPage.desc = selectedView.getDescriptionInputView().getText().toString().trim();

            if (selectedView.getModel().equalsModel(clonedPage)) {
                setEditEnabled();
            } else {
                if (clonedPage.id > 0) {
                    requestUpdatePage(clonedPage);
                } else {
                    selectedView.getModel().synchronize(clonedPage);
                    setEditEnabled();
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(e.getMessage(), e);
        }
    }

    private void updateTitle() {
        if (selectedView != null && selectedView.isEditEnabled()) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            toolbarTextView.setVisibility(View.GONE);
        } else {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbarTextView.setText(String.valueOf(selectedIndex + 1) + " of " + String.valueOf(model.pages.data.size()));
            toolbarTextView.setVisibility(View.VISIBLE);
        }
    }

    // ================================================================================================
    //  Class: Adapter
    // ================================================================================================

    private class Adapter extends RecyclerView.Adapter {
        private List<Page> items;
        private PageScrollView.Delegate delegate;

        public Adapter(PageScrollView.Delegate delegate) {
            this.delegate = delegate;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            PageScrollView view = new PageScrollView(viewGroup.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(viewGroup.getWidth(), viewGroup.getHeight());
            view.setLayoutParams(params);
            view.setDelegate(delegate);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            PageScrollView itemView = (PageScrollView) viewHolder.itemView;
            itemView.setModel(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        public void setItems(List<Page> items) {
            this.items = items;
            notifyDataSetChanged();
        }
    }

    // ================================================================================================
    //  Class: ViewHolder
    // ================================================================================================

    private final class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
