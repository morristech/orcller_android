package com.orcller.app.orcller.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.manager.AWSManager;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.model.album.Image;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcller.widget.UserPictureCropGuideView;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psfoundation.utils.URLUtils;
import pisces.psuikit.event.ImagePickerEvent;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/12/15.
 */
public class UserPictureEditActivity extends PSActionBarActivity {
    private static final String MEDIA_KEY = "media";
    private Media model;
    private ProgressDialog progressDialog;
    private SubsamplingScaleImageView imageView;
    private UserPictureCropGuideView guideView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_picture_edit);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        imageView = (SubsamplingScaleImageView) findViewById(R.id.imageView);
        guideView = (UserPictureCropGuideView) findViewById(R.id.guideView);

        imageView.setDoubleTapZoomScale(3f);
        imageView.setMaxScale(6f);
        imageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);

        setModel((Media) getIntent().getSerializableExtra(MEDIA_KEY));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Application.getTopActivity().getMenuInflater().inflate(R.menu.menu_user_picture_edit, menu);
        getSaveItem().setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                save();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        endDataLoading();

        imageView = null;
        model = null;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        if (progressDialog != null)
            progressDialog.hide();

        progressDialog = null;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(Media media) {
        Intent intent = new Intent(Application.applicationContext(), UserPictureEditActivity.class);
        intent.putExtra(MEDIA_KEY, media);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private MenuItem getSaveItem() {
        return getToolbar().getMenu().findItem(R.id.save);
    }

    private void setModel(Media model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        load();
    }

    private void load() {
        Image image = model.images.standard_resolution;

        imageView.recycle();

        if (URLUtils.isLocal(image.url))
            loadLocalFile(image);
        else
            loadWebFile(image);
    }

    private void loadLocalFile(final Image image) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                return BitmapFactory.decodeFile(image.url, options);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);

                imageView.setImage(ImageSource.cachedBitmap(result));
                getSaveItem().setEnabled(true);
            }
        }.execute();
    }

    private void loadWebFile(Image image) {
        ProgressBarManager.show();
        Glide.with(this)
                .load(SharedObject.toFullMediaUrl(image.url))
                .dontAnimate()
                .listener(new RequestListener<Object, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                        if (BuildConfig.DEBUG)
                            Log.e(e.getMessage(), e);

                        ProgressBarManager.hide();
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Bitmap bitmap = ((GlideBitmapDrawable) resource).getBitmap();
                        imageView.setImage(ImageSource.cachedBitmap(bitmap));
                        getSaveItem().setEnabled(true);
                        ProgressBarManager.hide();
                        return true;
                    }
                })
                .into(image.width, image.height);
    }

    private void save() {
        if (invalidDataLoading())
            return;

        final Runnable error = new Runnable() {
            @Override
            public void run() {
                endDataLoading();
                AlertDialogUtils.retry(R.string.m_fail_save_user_picture, new Runnable() {
                    @Override
                    public void run() {
                        save();
                    }
                });
            }
        };
        progressDialog = ProgressDialog.show(this, null, Application.applicationContext().getString(R.string.w_saving));

        UserDataProxy.getDefault().newUserPictureName(new Callback<ApiUsers.NewUserPictureNameRes>() {
            @Override
            public void onResponse(Response<ApiUsers.NewUserPictureNameRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    uploadImages(response.body().entity.user_picture);
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());

                    error.run();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                error.run();
            }
        });
    }

    private void uploadImages(final String filename) {
        final Runnable retry = new Runnable() {
            @Override
            public void run() {
                AlertDialogUtils.retry(R.string.m_fail_save_user_picture, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE)
                            uploadImages(filename);
                        else
                            endDataLoading();
                    }
                });
            }
        };

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                imageView.setDrawingCacheEnabled(true);

                Rect cropRect = guideView.getCropRect();
                return Bitmap.createBitmap(imageView.getDrawingCache(),
                        cropRect.left, cropRect.top, cropRect.right, cropRect.bottom - cropRect.top);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);

                imageView.setDrawingCacheEnabled(false);
                MediaManager.getDefault().saveUserPicture(result, filename, new MediaManager.CompleteHandler() {
                    @Override
                    public void onComplete(Error error) {
                        if (error == null) {
                            try {
                                User clonedUser = (User) AuthenticationCenter.getDefault().getUser().clone();
                                clonedUser.user_picture = filename;

                                endDataLoading();
                                AuthenticationCenter.getDefault().synchorinzeUser(clonedUser);
                            } catch (CloneNotSupportedException e) {
                                retry.run();
                            }
                        } else {
                            retry.run();
                        }
                    }
                });
            }
        }.execute();
    }
}
