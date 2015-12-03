package com.orcller.app.orcller.activity;

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
import android.view.WindowManager;
import android.widget.TextView;

import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Page;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.PageScrollView;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.model.APIResult;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;
import com.orcller.app.orcllermodules.utils.SoftKeyboardUtils;

import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSRecyclerViewPager;
import pisces.psuikit.imagepicker.OnScrollListener;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/3/15.
 */
public class PageListActivity extends PSActionBarActivity
        implements RecyclerViewPager.OnPageChangedListener, CommentInputView.Delegate, PageScrollView.Delegate {
    public static final String ALBUM_KEY = "album";
    public static final String SELECTED_INDEX_KEY = "selectedIndex";
    private int selectedIndex = -1;
    private Album model;
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

        toolbarTextView = (TextView) findViewById(R.id.toolbarTextView);
        recyclerView = (PSRecyclerViewPager) findViewById(R.id.recyclerView);
        commentInputView = (CommentInputView) findViewById(R.id.commentInputView);
        adapter = new Adapter(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnPageChangedListener(this);
        recyclerView.addOnScrollListener(new OnScrollListener());
        commentInputView.setDelegate(this);
        setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
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
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        SoftKeyboardNotifier.getDefault().unregister(this);
        stopSelectedVideoMediaView();
        recyclerView.removeOnPageChangedListener(this);
        recyclerView.removeAllViews();

        model = null;
        adapter = null;
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
                setEditEnabled();
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

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onClickCommentButton(PageScrollView target) {
        commentInputView.setFocus();
    }

    public void onClickHeartButton(PageScrollView target) {
        AlbumDataProxy.getDefault().likeOfPage(selectedView.getModel().id, new Callback<APIResult>() {
            @Override
            public void onResponse(Response<APIResult> response, Retrofit retrofit) {
                Log.d("response", response.body());
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    public void onClickPostButton() {
        Log.d("onClickPostButton", commentInputView.getText(), selectedView.getModel().id);
    }

    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                recyclerView.setScrollable(false);
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                recyclerView.setScrollable(true);
            }
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
                EventBus.getDefault().post(new OnChangeSelectedIndex(selectedIndex, selectedView));
        }
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
    //  Private
    // ================================================================================================

    private void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

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

    private void setEditEnabled() {
        if (selectedView == null)
            return;

        selectedView.setEditEnabled(!selectedView.isEditEnabled());
        recyclerView.setScrollable(!selectedView.isEditEnabled());
        commentInputView.setVisibility(selectedView.isEditEnabled() ? View.GONE : View.VISIBLE);
        SoftKeyboardUtils.hide(commentInputView);
        updateTitle();

        if (selectedView.isEditEnabled()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        Drawable icon = selectedView.isEditEnabled() ? null : getDrawable(R.drawable.icon_page_edit);
        String title = selectedView.isEditEnabled() ? getString(R.string.w_done) : null;
        MenuItem item = getToolbar().getMenu().findItem(R.id.edit);
        item.setIcon(icon);
        item.setTitle(title);
    }

    private void stopSelectedVideoMediaView() {
        if (selectedView != null) {
            if (selectedView.getVideoMediaView() != null)
                selectedView.getVideoMediaView().stop();
        }
    }

    private void updateTitle() {
        if (selectedView != null && selectedView.isEditEnabled()) {
            toolbarTextView.setText(getString(R.string.w_title_edit_page));
        } else {
            String text = String.valueOf(selectedIndex + 1) + " of " + String.valueOf(model.pages.data.size());
            toolbarTextView.setText(text);
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

    // ================================================================================================
    //  Class: OnChangeSelectedIndex
    // ================================================================================================

    public static class OnChangeSelectedIndex {
        private int selectedIndex;
        private PageScrollView selectedView;

        public OnChangeSelectedIndex(int selectedIndex, PageScrollView selectedView) {
            this.selectedIndex = selectedIndex;
            this.selectedView = selectedView;
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        public PageScrollView getSelectedView() {
            return selectedView;
        }
    }
}
