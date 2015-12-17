package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.orcller.app.orcller.manager.ImagePickerManager;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.Log;
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

        imageView.setBackgroundColor(Color.BLACK);
        imageView.setDoubleTapZoomScale(3f);
        imageView.setMaxScale(6f);
        setModel((User) getIntent().getSerializableExtra(USER_KEY));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (model.isMe()) {
            Application.getTopActivity().getMenuInflater().inflate(R.menu.menu_user_picture, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                ImagePickerManager.getDefault().pick(this, true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
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

    public void onEventMainThread(Object event) {
        if (event instanceof Model.Event) {
            Model.Event casted = (Model.Event) event;

            if (Model.Event.SYNCHRONIZE.equals(casted.getType()) &&
                    casted.getTarget().equals(AuthenticationCenter.getDefault().getUser()) &&
                    model.isMe()) {
                User user = (User) casted.getTarget();
                model.user_name = user.user_name;
                model.user_picture = user.user_picture;

                ImagePickerManager.getDefault().clear();
                Application.moveToBack(this);
                modelChanged();
            }
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setModel(User model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        EventBus.getDefault().register(this);
        modelChanged();
    }

    private void modelChanged() {
        toolbarTextView.setText(model.user_id);
        ProgressBarManager.show();
        imageView.recycle();
        Glide.with(this)
                .load(SharedObject.toUserPictureUrl(model.user_picture, SharedObject.SizeType.Large))
                .listener(new RequestListener<Object, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                        imageView.setImage(ImageSource.resource(R.drawable.profile_noimage320));
                        imageView.setPanEnabled(false);
                        imageView.setZoomEnabled(false);
                        ProgressBarManager.hide();
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Bitmap bitmap = ((GlideBitmapDrawable) resource).getBitmap();
                        imageView.setImage(ImageSource.cachedBitmap(bitmap));
                        imageView.setPanEnabled(true);
                        imageView.setZoomEnabled(true);
                        ProgressBarManager.hide();
                        return true;
                    }
                })
                .into(MediaManager.WIDTH_IMAGE_STANDARD_RESOLUTION, MediaManager.WIDTH_IMAGE_STANDARD_RESOLUTION);
    }
}