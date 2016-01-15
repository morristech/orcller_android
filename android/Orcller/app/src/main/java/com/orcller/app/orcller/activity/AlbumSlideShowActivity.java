package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Image;
import com.orcller.app.orcller.model.Page;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;

import java.io.File;
import java.net.URL;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psfoundation.utils.URLUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.keyboard.SoftKeyboardNotifier;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 1/15/16.
 */
public class AlbumSlideShowActivity extends PSActionBarActivity implements
        AlbumFlipView.Delegate, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String ALBUM_KEY = "album";
    private Album model;
    private FrameLayout rootLayout;
    private ProgressBar progressBar;
    private AlbumFlipView albumFlipView;

    // ================================================================================================
    //  Overridden: AlbumCreateActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_slide_show);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        rootLayout = (FrameLayout) findViewById(R.id.rootLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        albumFlipView = (AlbumFlipView) findViewById(R.id.albumFlipView);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        albumFlipView.setDelegate(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_slide_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cycle:
                item.setChecked(!item.isChecked());
                item.setIcon(item.isChecked() ? R.drawable.icon_menu_cycle_selected : R.drawable.icon_menu_cycle_normal);
                return true;

            case R.id.control:
                playOrPause();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide();
    }

    @Override
    public boolean invalidDataLoading() {
        boolean invalid = super.invalidDataLoading();

        if (!invalid)
            ProgressBarManager.show();

        return invalid;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Album album) {
        Intent intent = new Intent(Application.applicationContext(), AlbumSlideShowActivity.class);
        intent.putExtra(ALBUM_KEY, album);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * ViewTreeObserver.OnGlobalLayoutListener
     */
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        albumFlipView.setPageWidth(rootLayout.getWidth() / 2);
        albumFlipView.setPageHeight(rootLayout.getWidth() / 2);
        setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
    }

    /**
     * AlbumFlipView.Delegate
     */
    public void onCancelPanning(AlbumFlipView view) {

    }

    public void onChangePageIndex(AlbumFlipView view, int pageIndex) {

    }

    public void onLoadRemainPages(AlbumFlipView view) {

    }

    public void onPlay(AlbumFlipView view) {

    }

    public void onPause(AlbumFlipView view) {

    }

    public void onStartLoadRemainPages(AlbumFlipView view) {

    }

    public void onStartPanning(AlbumFlipView view) {

    }

    public void onStop(AlbumFlipView view) {
        albumFlipView.setPageIndex(0);

        if (getCycleMenuItem().isChecked()) {
            albumFlipView.play();
        }
    }

    public void onTap(AlbumFlipView view, FlipView flipView, PageView pageView) {

    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private MenuItem getControlMenuItem() {
        return getToolbar().getMenu().findItem(R.id.control);
    }

    private MenuItem getCycleMenuItem() {
        return getToolbar().getMenu().findItem(R.id.cycle);
    }

    private void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        prepare();
    }

    private void playOrPause() {
        MenuItem item = getControlMenuItem();

        item.setChecked(!item.isChecked());
        item.setIcon(item.isChecked() ? R.drawable.icon_menu_pause_normal : R.drawable.icon_menu_play_normal);

        if (albumFlipView.isPlaying()) {
            albumFlipView.pause();
        } else {
            albumFlipView.play();
        }
    }

    private void prepare() {
        final Point p = new Point(0, model.pages.total_count);

        final Runnable progress = new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(Math.round(++p.x * 100 / p.y));

                if (p.x >= p.y) {
                    progressBar.setVisibility(View.GONE);
                    albumFlipView.setVisibility(View.VISIBLE);
                    albumFlipView.setModel(model);
                    albumFlipView.setPageIndex(0);
                }
            }
        };

        AlbumDataProxy.getDefault().remainPages(model, new AlbumDataProxy.CompleteHandler() {
            @Override
            public void onComplete(boolean isSuccess) {
                for (Page page : model.pages.data) {
                    loadImage(page.media.images.standard_resolution, progress);
                }
            }
        });
    }

    private void loadImage(Image image, final Runnable runnable) {
        try {
            final Object source = URLUtils.isLocal(image.url) ? new File(image.url) : new URL(SharedObject.toFullMediaUrl(image.url));

            Glide.with(this)
                    .load(source)
                    .listener(new RequestListener<Object, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                            if (BuildConfig.DEBUG)
                                Log.e("loadImage", source, e);

                            runnable.run();

                            return true;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            runnable.run();
                            return true;
                        }
                    })
                    .into(image.width, image.height);
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e("loadImage", image.url, e);
        }
    }
}