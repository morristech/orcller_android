package com.orcller.app.orcller.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Comments;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.model.Page;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.MediaView;
import com.orcller.app.orcller.widget.PageScrollView;

import pisces.psfoundation.model.Resources;
import pisces.psuikit.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.model.ApiResult;
import pisces.psuikit.utils.AlertDialogUtils;
import pisces.psuikit.keyboard.SoftKeyboardNotifier;

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
    public static final String PAGE_ID_KEY = "pageId";
    public static final String SELECTED_INDEX_KEY = "selectedIndex";
    private int selectedIndex = -1;
    private ArrayList<Media> mediaList = new ArrayList<>();
    private Adapter adapter;
    private Album model;
    private LinearLayout rootLayout;
    private TextView toolbarTextView;
    private ProgressDialog progressDialog;
    private PSRecyclerViewPager recyclerView;
    private PageScrollView selectedView;
    private CommentInputView commentInputView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pagelist);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle(getString(R.string.w_title_edit_page));

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        toolbarTextView = (TextView) findViewById(R.id.toolbarTextView);
        recyclerView = (PSRecyclerViewPager) findViewById(R.id.recyclerView);
        commentInputView = (CommentInputView) findViewById(R.id.commentInputView);
        adapter = new Adapter(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnPageChangedListener(this);
        recyclerView.addOnScrollListener(new OnScrollListener());
        commentInputView.setCommentType(CommentInputView.COMMENT_TYPE_PAGE);
        commentInputView.setDelegate(this);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        SoftKeyboardNotifier.getDefault().register(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (model != null && model.isMine() || selectedView != null && selectedView.getModel().isMine()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_pagelist, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (model == null || selectedView == null)
            return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.edit:
                if (selectedView.isEditEnabled()) {
                    updatePage();
                } else {
                    setEditEnabled();
                }
                return true;

            case android.R.id.home:
                if (selectedView.isEditEnabled()) {
                    setEditEnabled();
                } else {
                    onBackPressed();
                }
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        setRecyclerViewLayout();

        if (getIntent().getSerializableExtra(ALBUM_KEY) != null) {
            setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
        } else if (getIntent().getLongExtra(PAGE_ID_KEY, 0) > 0) {
            load(getIntent().getLongExtra(PAGE_ID_KEY, 0));
        }
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (selectedView != null && selectedView.isEditEnabled()) {
                    setEditEnabled();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide();

        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog = null;
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album model, int selectedIndex) {
        Intent intent = new Intent(Application.applicationContext(), PageListActivity.class);
        intent.putExtra(ALBUM_KEY, model);
        intent.putExtra(SELECTED_INDEX_KEY, selectedIndex);
        Application.getTopActivity().startActivity(intent);
    }

    public static void show(long pageId) {
        Intent intent = new Intent(Application.applicationContext(), PageListActivity.class);
        intent.putExtra(PAGE_ID_KEY, pageId);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * EventBus listener
     */
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

    /**
     * PageScrollView.Delegate
     */
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
                if (response.isSuccess() && response.body().isSuccess()) {
                    page.likes.synchronize(response.body().entity, true);
                } else {
                    if (BuildConfig.DEBUG)
                        Log.d("Api Error", response.body());
                }

                endDataLoading();
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.d("onFailure", t);

                endDataLoading();
            }
        });

        page.likes.participated();
        selectedView.updateButtons();
    }

    public void onClickMediaView(PageScrollView target, MediaView mediaView) {
        MediaListActivity.show(mediaList, selectedIndex);
    }

    public void onDescriptionTextChanged(PageScrollView target, String text) {
        getEditMenuItem().setEnabled(!ObjectUtils.equals(selectedView.getModel().desc, text));
    }

    /**
     * CommentInputView.Delegate
     */
    public void onCompletePost(CommentInputView commentInputView, Comments comments) {
        selectedView.addComments(comments);
    }

    /**
     * PSRecyclerViewPager listener
     */
    public void OnPageChanged(int oldPosition, int newPosition) {
        boolean initialSelection = newPosition == selectedIndex;

        setSelectedIndex(newPosition);

        if (selectedView == null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedIndex);

            if (viewHolder != null) {
                selectedView = (PageScrollView) viewHolder.itemView;
                commentInputView.setModel(selectedView.getModel().comments, selectedView.getModel().id);
                updateTitle();
                invalidateOptionsMenu();
            }

            if (!initialSelection)
                EventBus.getDefault().post(new IndexChangeEvent(
                        IndexChangeEvent.INDEX_CHANGE, this, selectedView, selectedIndex));
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private MenuItem getEditMenuItem() {
        return getToolbar().getMenu().findItem(R.id.edit);
    }

    private void setModel(final Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        Application.run(new Runnable() {
            @Override
            public void run() {
                mediaList.clear();

                for (Page page : model.pages.data) {
                    mediaList.add(page.media);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                modelChanged();
                loadRemainPages();
            }
        });
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
    }

    private void load(final long pageId) {
        if (invalidDataLoading())
            return;

        if (isFirstLoading())
            ProgressBarManager.show();

        AlbumDataProxy.getDefault().viewByPageId(pageId, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(final Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    final Album album = response.body().entity;
                    new AsyncTask<Void, Void, Integer>() {
                        @Override
                        protected Integer doInBackground(Void... params) {
                            int i = 0;
                            for (Page page : album.pages.data) {
                                if (page.id == pageId)
                                    return i;
                                i++;
                            }
                            return 0;
                        }

                        @Override
                        protected void onPostExecute(Integer result) {
                            super.onPostExecute(result);

                            setModel(album);
                            setSelectedIndex(result);
                            endDataLoading();
                        }
                    }.execute();
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

    public void loadRemainPages() {
        if (model == null || model.pages.data.size() >= model.pages.total_count || invalidDataLoading())
            return;

        AlbumDataProxy.getDefault().remainPages(model, new AlbumDataProxy.CompleteHandler() {
            @Override
            public void onComplete(boolean isSuccess) {
                if (isSuccess) {
                    Application.run(new Runnable() {
                        @Override
                        public void run() {
                            mediaList.clear();

                            for (Page page : model.pages.data) {
                                mediaList.add(page.media);
                            }
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            updateTitle();
                            adapter.setItems(model.pages.data);
                        }
                    });
                }

                endDataLoading();
            }
        });
    }

    private void modelChanged() {
        adapter.setItems(model.pages.data);
        setSelectedIndex(getIntent().getIntExtra(SELECTED_INDEX_KEY, 0));
    }

    private void requestUpdatePage(final Page page) {
        if (invalidDataLoading())
            return;

        progressDialog = ProgressDialog.show(this, null, getString(R.string.w_processing));

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
                    selectedView.getModel().synchronize(page, true);
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

        Drawable icon = selectedView.isEditEnabled() ? null : Resources.getDrawable(R.drawable.icon_page_edit);
        String title = selectedView.isEditEnabled() ? getString(R.string.w_save) : null;
        MenuItem item = getEditMenuItem();
        item.setEnabled(!selectedView.isEditEnabled());
        item.setIcon(icon);
        item.setTitle(title);
    }

    private void setRecyclerViewLayout() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
        params.bottomMargin = commentInputView.getVisibility() == View.VISIBLE ? commentInputView.getHeight() : 0;
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
                    selectedView.getModel().synchronize(clonedPage, true);
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
            toolbarTextView.setText(String.valueOf(selectedIndex + 1) + " of " + String.valueOf(model.pages.total_count));
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
