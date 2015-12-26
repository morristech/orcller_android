package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.itemview.AlbumItemView;
import com.orcller.app.orcller.manager.AlbumOptionsManager;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.AlbumItemViewDelegate;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.CoeditButton;
import com.orcller.app.orcller.widget.CommentInputView;
import com.orcller.app.orcller.widget.ContributorListView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSScrollView;
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
        implements AlbumItemViewDelegate.Invoker, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String ALBUM_KEY = "album";
    private static final String ALBUM_ID_KEY = "albumId";
    private Album model;
    private AlbumItemViewDelegate albumItemViewDelegate;
    private AlbumOptionsManager albumOptionsManager;
    private LinearLayout rootLayout;
    private PSScrollView scrollView;
    private AlbumItemView albumItemView;
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
        albumItemViewDelegate.setCommentActionType(AlbumItemViewDelegate.COMMENT_ACTION_COMMENTS);
        coeditButton.setSelected(true);
        coeditButton.setTextSize(GraphicUtils.convertDpToPixel(13));
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        SoftKeyboardNotifier.getDefault().register(this);
        EventBus.getDefault().register(this);
    }

    @Override
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (albumOptionsManager != null)
            return albumOptionsManager.onCreateOptionsMenu(menu);
        return false;
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
            albumItemView.getAlbumFlipView().setPageIndex(
                    SharedObject.convertPositionToPageIndex(casted.getSelectedIndex()));
        }
    }

    /**
     * AlbumItemViewDelete.Invoker
     */
    public CommentInputView getCommentInputView() {
        return null;
    }

    public void onChangePanningState(boolean isPanning) {
        scrollView.setScrollable(!isPanning);
    }

    public void onTap(AlbumFlipView view, FlipView flipView, PageView pageView) {
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
        setScrollViewLayout();
        invalidateOptionsMenu();

        try {
            coeditButton.setModel(model, model.id);
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(e.getMessage());
        }
    }

    private void setScrollViewLayout() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
        params.bottomMargin = coeditButton.getVisibility() == View.VISIBLE ? coeditButton.getHeight() : 0;
    }
}