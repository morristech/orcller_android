package com.orcller.app.orcller.manager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.AlbumEditActivity;
import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.event.AlbumEvent;
import com.orcller.app.orcller.itemview.PublicSettingsAlertDialogView;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.proxy.FBShareProxy;
import com.orcller.app.orcller.proxy.TimelineDataProxy;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.ext.PSObject;
import pisces.psfoundation.utils.Log;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/8/15.
 */
public class AlbumOptionsManager extends PSObject {
    private Context context;
    private Album album;
    private ProgressDialog progressDialog;

    public AlbumOptionsManager(Context context, Album album) {
        this.context = context;
        this.album = album;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog = null;
        }
    }

    @Override
    public boolean invalidDataLoading() {
        boolean invalid = super.invalidDataLoading();

        if (!invalid)
            progressDialog = ProgressDialog.show(context, null, context.getString(R.string.w_processing));

        return invalid;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean onCreateOptionsMenu(Menu menu) {
        return onCreateOptionsMenu(menu, null);
    }

    public boolean onCreateOptionsMenu(View anchor) {
        return onCreateOptionsMenu(null, anchor);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                AlbumEditActivity.show(album.id);
                break;

            case R.id.delete:
                showDeleteAlertDialog();
                break;

            case R.id.publicSettings:
                publicSettings();
                break;

            case R.id.share:
                share();
                break;

            case R.id.slideShow:
                //TODO : Impl open slideshow activity
                break;

            case R.id.hide:
                hide();
                break;

            case R.id.hideAll:
                hideAll();
                break;

            case R.id.report:
                report();
                break;
        }

        return true;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void chagePermission(final Album.Permission permission) {
        if (invalidDataLoading())
            return;

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chagePermission(permission);
            }
        };

        try {
            Album clonedAlbum = (Album) album.clone();
            clonedAlbum.permission = permission.value();

            AlbumDataProxy.getDefault().update(clonedAlbum, new Callback<ApiAlbum.AlbumRes>() {
                @Override
                public void onResponse(Response<ApiAlbum.AlbumRes> response, Retrofit retrofit) {
                    if (response.isSuccess() && response.body().isSuccess()) {
                        endDataLoading();
                        album.synchronize(response.body().entity, true);
                    } else {
                        showFailAlertDialog(runnable, response.body());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    showFailAlertDialog(runnable, t);
                }
            });
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(e.getMessage(), e);
        }
    }

    private void delete() {
        if (invalidDataLoading())
            return;

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                delete();
            }
        };

        AlbumDataProxy.getDefault().delete(album.id, new Callback<ApiResult>() {
            @Override
            public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    endDataLoading();
                    EventBus.getDefault().post(new AlbumEvent(AlbumEvent.DELETE, this, album));
                    finishActivity();
                } else {
                    showFailAlertDialog(runnable, response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                showFailAlertDialog(runnable, t);
            }
        });
    }

    private void finishActivity() {
        if (Application.getTopActivity() instanceof AlbumViewActivity)
            Application.getTopActivity().finish();
    }

    private void hide() {
        if (invalidDataLoading())
            return;

        final Object target = this;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                hide();
            }
        };

        TimelineDataProxy.getDefault().hideAlbum(album.id, new Callback<ApiResult>() {
            @Override
            public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    endDataLoading();
                    EventBus.getDefault().post(new AlbumEvent(AlbumEvent.DELETE, target, album));
                    finishActivity();
                } else {
                    showFailAlertDialog(runnable, response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                showFailAlertDialog(runnable, t);
            }
        });
    }

    private void hideAll() {
        if (invalidDataLoading())
            return;

        final Object target = this;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                hideAll();
            }
        };

        TimelineDataProxy.getDefault().hideAllAlbums(album.user_uid, new Callback<ApiResult>() {
            @Override
            public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    endDataLoading();
                    EventBus.getDefault().post(new AlbumEvent(AlbumEvent.MODIFY, target, album));
                    finishActivity();
                } else {
                    showFailAlertDialog(runnable, response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                showFailAlertDialog(runnable, t);
            }
        });
    }

    private boolean onCreateOptionsMenu(Menu menu, View view) {
        if (album == null)
            return false;

        if (menu != null) {
            setMenuItems(menu, Application.getTopActivity().getMenuInflater());
        } else {
            PopupMenu popupMenu  = new PopupMenu(context, view);
            setMenuItems(popupMenu.getMenu(), popupMenu.getMenuInflater());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
            popupMenu.show();
        }

        return true;
    }

    private void publicSettings() {
        final PublicSettingsAlertDialogView view = new PublicSettingsAlertDialogView(context);

        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.WhiteThemeAlertDialog))
                .setTitle(R.string.w_public_settings)
                .setView(view)
                .setNegativeButton(R.string.w_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.w_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showChangePermissionAlertDialog(view.getCheckedPermission());
                    }
                })
                .create()
                .show();
        view.setModel(album);
    }

    private void report() {
        if (invalidDataLoading())
            return;

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                report();
            }
        };

        AlbumDataProxy.getDefault().report(album.id, Album.ReportType.Inappropriate, new Callback<ApiResult>() {
            @Override
            public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    endDataLoading();
                    AlertDialogUtils.show(R.string.m_complete_report_send, R.string.w_ok);
                } else {
                    showFailAlertDialog(runnable, response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                showFailAlertDialog(runnable, t);
            }
        });
    }

    private void setMenuItems(Menu menu, MenuInflater inflater) {
        if (album.isMine()) {
            inflater.inflate(R.menu.menu_album_view_owner, menu);
        } else if (album.contributors.isParticipated()) {
            inflater.inflate(R.menu.menu_album_view_coedit, menu);
        } else {
            inflater.inflate(R.menu.menu_album_view, menu);
            MenuItem item = menu.findItem(R.id.hideAll);
            item.setTitle(item.getTitle() + " " + album.user_id);
        }
    }

    private void share() {
        FBShareProxy.getDefault().share(album);
    }

    private void showChangePermissionAlertDialog(final Album.Permission permission) {
        if (Album.Permission.Private.equals(permission)) {
            AlertDialogUtils.show(context.getString(R.string.m_warn_album_permission),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == AlertDialog.BUTTON_POSITIVE) {
                                chagePermission(permission);
                            }
                        }
                    },
                    context.getString(R.string.w_cancel),
                    context.getString(R.string.w_change));
        } else {
            chagePermission(permission);
        }
    }

    private void showDeleteAlertDialog() {
        AlertDialogUtils.show(context.getString(R.string.m_confirm_album_delete),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == AlertDialog.BUTTON_POSITIVE) {
                            delete();
                        }
                    }
                },
                context.getString(R.string.w_cancel),
                context.getString(R.string.w_delete));
    }

    private void showFailAlertDialog(final Runnable retry, Throwable t) {
        if (BuildConfig.DEBUG)
            Log.e("onFailure", t);

        showFailAlertDialog(retry);
    }

    private void showFailAlertDialog(final Runnable retry, ApiResult result) {
        if (BuildConfig.DEBUG)
            Log.e("Api Error", result);

        showFailAlertDialog(retry);
    }

    private void showFailAlertDialog(final Runnable retry) {
        endDataLoading();
        AlertDialogUtils.retry(R.string.m_fail_common, retry);
    }
}
