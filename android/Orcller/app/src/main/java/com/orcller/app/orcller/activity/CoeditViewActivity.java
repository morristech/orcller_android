package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.event.AlbumEvent;
import com.orcller.app.orcller.itemview.AlbumItemView;
import com.orcller.app.orcller.itemview.TempAlbumItemView;
import com.orcller.app.orcller.manager.AlbumOptionsManager;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.manager.MediaUploadUnit;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.Contributors;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.AlbumItemViewDelegate;
import com.orcller.app.orcller.widget.AlbumInfoProfileView;
import com.orcller.app.orcller.widget.AlbumView;
import com.orcller.app.orcller.widget.CoeditButton;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.ContributorListView;
import com.orcller.app.orcller.widget.PageView;
import com.orcller.app.orcller.widget.TemplateView;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.event.Event;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSScrollView;
import pisces.psuikit.keyboard.SoftKeyboardNotifier;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/14/15.
 */
public class CoeditViewActivity extends PSActionBarActivity
        implements AlbumItemViewDelegate.Invoker, CoeditButton.Delegate, ContributorListView.Delegate, TempAlbumItemView.Delegate, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String ALBUM_KEY = "album";
    private static final String ALBUM_ID_KEY = "albumId";
    private Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    private Album model;
    private AlbumItemViewDelegate albumItemViewDelegate;
    private AlbumOptionsManager albumOptionsManager;
    private LinearLayout rootLayout;
    private PSScrollView scrollView;
    private AlbumItemView albumItemView;
    private TempAlbumItemView tempAlbumItemView;
    private ContributorListView contributorListView;
    private CoeditButton coeditButton;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_coedit_view);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);

        albumItemViewDelegate = new AlbumItemViewDelegate(this);
        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        scrollView = (PSScrollView) findViewById(R.id.scrollView);
        albumItemView = (AlbumItemView) findViewById(R.id.albumItemView);
        contributorListView = (ContributorListView) findViewById(R.id.contributorListView);
        coeditButton = (CoeditButton) findViewById(R.id.button);

        albumItemView.setDelegate(albumItemViewDelegate);
        albumItemView.setButtonVisiblity(AlbumItemView.HEART | AlbumItemView.COMMENT | AlbumItemView.STAR);
        albumItemViewDelegate.setCommentActionType(AlbumItemViewDelegate.COMMENT_ACTION_OPEN_COMMENTS);
        contributorListView.setDelegate(this);
        coeditButton.setDelegate(this);
        coeditButton.setSelected(true);
        coeditButton.setTextSize(GraphicUtils.convertDpToPixel(13));
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        SoftKeyboardNotifier.getDefault().register(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (albumOptionsManager != null)
            return albumOptionsManager.onCreateOptionsMenu(menu);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();

        dequeueEvent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return super.onOptionsItemSelected(item);
            default:
                return albumOptionsManager.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        SoftKeyboardNotifier.getDefault().unregister(this);
        ProgressBarManager.hide(this);
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album album) {
        Intent intent = new Intent(Application.applicationContext(), CoeditViewActivity.class);
        intent.putExtra(ALBUM_KEY, album);
        Application.getTopActivity().startActivity(intent);
    }

    public static void show(long albumId) {
        Intent intent = new Intent(Application.applicationContext(), CoeditViewActivity.class);
        intent.putExtra(ALBUM_ID_KEY, albumId);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof IndexChangeEvent) {
            IndexChangeEvent casted = (IndexChangeEvent) event;
            albumItemView.getAlbumView().setPageIndex(
                    SharedObject.convertPositionToPageIndex(casted.getSelectedIndex()));
        } else if (event instanceof AlbumEvent) {
            AlbumEvent casted = (AlbumEvent) event;

            if (AlbumEvent.MODIFY.equals(casted.getType())) {
                eventQueue.offer((Event) event);
                dequeueEvent();
            }
        } else if (event instanceof MediaUploadUnit.Event) {
            MediaUploadUnit.Event casted = (MediaUploadUnit.Event) event;

            if (MediaUploadUnit.Event.START_UPLOADING.equals(casted.getType())) {
                eventQueue.offer((Event) event);
                dequeueEvent();
            }
        }
    }

    /**
     * ViewTreeObserver.OnGlobalLayoutListener
     */
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        if (getIntent().getSerializableExtra(ALBUM_KEY) != null) {
            setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
        } else if (getIntent().getLongExtra(ALBUM_ID_KEY, 0) > 0) {
            load(getIntent().getLongExtra(ALBUM_ID_KEY, 0));
        }
    }


    /**
     * CoeditButton.Delegate
     */
    public void onChange(CoeditButton target, Contributors contributors) {
        setScrollViewLayout();
    }

    public void onSync(CoeditButton target, Contributors contributors) {
        setScrollViewLayout();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * AlbumItemViewDelete.Invoker
     */
    public CommentInputView getCommentInputView() {
        return null;
    }

    public void onAlbumInfoSynchronize(AlbumItemView itemView, AlbumAdditionalListEntity model) {
    }

    public void onAlbumSynchronize(AlbumItemView itemView) {
    }

    public void onChangePanningState(boolean isPanning) {
        scrollView.setScrollable(!isPanning);
    }

    public void onTap(AlbumView view) {
    }

    public void onTapTemplateView(AlbumView view, TemplateView templateView, PageView pageView) {
    }

    /**
     * ContributorListView.Delegate
     */
    public void onFailure(ContributorListView listView, Error error) {
        ProgressBarManager.hide((ViewGroup) listView.getParent());
    }

    public void onLoad(ContributorListView listView) {
        if (listView.isFirstLoading())
            ProgressBarManager.show((ViewGroup) listView.getParent());
    }

    public void onLoadComplete(ContributorListView listView) {
        ProgressBarManager.hide((ViewGroup) listView.getParent());
    }

    /**
     * TempAlbumItemView.Delegate
     */
    public void onClickCancelButton(TempAlbumItemView itemView) {
        clearUnit(itemView.getUnit());
    }

    public void onClickDeleteButton(TempAlbumItemView itemView) {
        clearUnit(itemView.getUnit());
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

    private void clearUnit(MediaUploadUnit unit) {
        MediaManager.getDefault().clearItem(unit);

        if (tempAlbumItemView != null) {
            ViewGroup parent = (ViewGroup) albumItemView.getParent();
            parent.removeView(tempAlbumItemView);
            tempAlbumItemView = null;
        }

        albumItemView.setVisibility(View.VISIBLE);
    }

    private void dequeueEvent() {
        if (!isActive() || eventQueue.size() < 1)
            return;

        Event event = eventQueue.poll();
        String type = event.getType();
        Object target = event.getTarget();
        ViewGroup parent = (ViewGroup) albumItemView.getParent();

        if (MediaUploadUnit.Event.START_UPLOADING.equals(type)) {
            final MediaUploadUnit unit = (MediaUploadUnit) target;

            if (MediaUploadUnit.UploadState.Cancelled.equals(unit.getUploadState())) {
                dequeueEvent();
            } else {
                if (!MediaUploadUnit.CompletionState.None.equals(unit.getCompletionState())) {
                    if (tempAlbumItemView == null) {
                        albumItemView.setVisibility(View.GONE);
                        tempAlbumItemView = new TempAlbumItemView(this);
                        tempAlbumItemView.setDescriptionMode(AlbumInfoProfileView.USER_NICKNAME);
                        tempAlbumItemView.setDelegate(this);
                        tempAlbumItemView.setUnit(unit);
                        parent.addView(tempAlbumItemView, 0, new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        getSupportActionBar().setTitle(unit.getModel().name);
                    }
                }

                dequeueEvent();
            }
        } else if (target instanceof MediaUploadUnit && AlbumEvent.MODIFY.equals(type)) {
            if (tempAlbumItemView != null) {
                parent.removeView(tempAlbumItemView);
                tempAlbumItemView = null;
            }

            setModel((Album) event.getObject());
            albumItemView.setVisibility(View.VISIBLE);
            dequeueEvent();
        }
    }

    private void load(long albumId) {
        if (invalidDataLoading())
            return;

        ProgressBarManager.show();

        AlbumDataProxy.getDefault().view(albumId, new Callback<ApiAlbum.AlbumRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                endDataLoading();

                if (response.isSuccess() && response.body().isSuccess()) {
                    setModel(response.body().entity);
                } else {
                    if (DEBUG)
                        e("Api Error", response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                endDataLoading();

                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);
            }
        });
    }

    private void modelChanged() {
        albumOptionsManager = new AlbumOptionsManager(this, model);
        getSupportActionBar().setTitle(model.name);
        albumItemView.setModel(model);
        contributorListView.setDataType(model.isMine() ? ContributorListView.STANDBY : ContributorListView.CONTRIBUTORS);
        contributorListView.setModel(model);
        scrollView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();

        try {
            coeditButton.setModel(model, model.id);
            setScrollViewLayout();
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(e.getMessage());
        }
    }

    private void setScrollViewLayout() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
        RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) coeditButton.getLayoutParams();
        params.bottomMargin = coeditButton.getVisibility() == View.VISIBLE ?
                coeditButton.getHeight() + buttonParams.topMargin + buttonParams.bottomMargin : 0;
    }
}