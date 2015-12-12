package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.widget.ProfileContentView;
import com.orcller.app.orcller.widget.ProfileHearderView;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import java.util.ArrayList;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 12/12/15.
 */
public class UserPictureActivity extends PSActionBarActivity {
    private static final String USER_KEY = "user";
    private User model;
    private TextView toolbarTextView;
    private SubsamplingScaleImageView imageView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_picture);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTextView = (TextView) findViewById(R.id.toolbarTextView);
        imageView = (SubsamplingScaleImageView) findViewById(R.id.imageView);

        setModel((User) getIntent().getSerializableExtra(USER_KEY));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressBarManager.hide(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(User user) {
        Intent intent = new Intent(Application.applicationContext(), UserPictureActivity.class);
        intent.putExtra(USER_KEY, user);
        Application.startActivity(intent, R.animator.fadein, R.animator.fadeout);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setModel(User model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;
        toolbarTextView.setText(model.user_id);

        Glide.with(this)
                .load(SharedObject.toUserPictureUrl(model.user_picture, SharedObject.SizeType.Large))
                .listener(new RequestListener<Object, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Bitmap bitmap = ((GlideBitmapDrawable) resource).getBitmap();
                        imageView.setImage(ImageSource.cachedBitmap(bitmap));
                        return true;
                    }
                })
                .into(MediaManager.WIDTH_IMAGE_STANDARD_RESOLUTION, MediaManager.WIDTH_IMAGE_STANDARD_RESOLUTION);
    }
}