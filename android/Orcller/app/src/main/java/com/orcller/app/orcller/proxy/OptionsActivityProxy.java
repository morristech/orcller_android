package com.orcller.app.orcller.proxy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.ChangePasswordActivity;
import com.orcller.app.orcller.activity.OptionsActivity;
import com.orcller.app.orcller.dialog.ConnectedAccountsAlertDialogView;
import com.orcller.app.orcller.dialog.PublicSettingsAlertDialogView;
import com.orcller.app.orcller.dialog.PushSettingsAlertDialogView;
import com.orcller.app.orcller.dialog.VersionInfoAlertDialogView;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;
import com.orcller.app.orcllermodules.activity.WebViewActivity;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import pisces.psuikit.utils.AlertDialogUtils;

import java.util.HashMap;
import java.util.Map;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.ext.PSObject;
import pisces.psfoundation.model.Resources;
import pisces.psuikit.manager.ProgressDialogManager;
import pisces.psuikit.widget.SectionedListView;

/**
 * Created by pisces on 12/24/15.
 */
public class OptionsActivityProxy extends PSObject implements SectionedListView.Delegate {
    private OptionsActivity invoker;

    public OptionsActivityProxy(OptionsActivity invoker) {
        this.invoker = invoker;

        invoker.getListView().setDelegate(this);
    }

    // ================================================================================================
    //  Overridden: PSObject
    // ================================================================================================

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressDialogManager.hide();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * SectionedListView.Delegate
     */
    public boolean isEnabled(ListView listView, SectionedListView.IndexPath indexPath) {
        return true;
    }

    public void onItemClick(ListView listView, View view, SectionedListView.IndexPath indexPath) {
        switch (indexPath.section()) {
            case 0:
                if (indexPath.row() == 0) {
                    openConnectedAccountsAlertDialog();
                } else if (indexPath.row() == 1) {
                    Intent intent = new Intent(invoker, ChangePasswordActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Application.applicationContext().startActivity(intent);
                } else {
                    openPublicSettingsAlertDialog();
                }
                break;

            case 1:
                if (indexPath.row() == 0) {
                    openPushSettingsAlertDialog();
                } else if (indexPath.row() == 1) {
                    openVersionInfoAlertDialog();
                }
                break;

            case 2:
                if (indexPath.row() == 0)
                    openWebViewActivity(R.string.path_private_polcy, R.string.w_private_policy);
                else
                    openWebViewActivity(R.string.path_terms, R.string.w_term_of_service);
                break;

            case 3:
                logout();
                break;
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void changePermission(final Album.Permission permission) {
        if (invalidDataLoading(R.string.w_saving))
            return;

        final User user = AuthenticationCenter.getDefault().getUser();
        ApiMember.UpdateUserOptionsReq req = new ApiMember.UpdateUserOptionsReq();
        req.user_options_album_permission = permission.value();
        req.user_options_pns_types = user.user_options.pns_types;

        AuthenticationCenter.getDefault().updateUserOptions(req, new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                endDataLoading();

                if (error == null) {
                    invoker.getListView().reload();
                } else {
                    AlertDialogUtils.retry(R.string.m_fail_change, new Runnable() {
                        @Override
                        public void run() {
                            changePermission(permission);
                        }
                    });
                }
            }
        });
    }

    private boolean invalidDataLoading(int messageResId) {
        boolean invalid = super.invalidDataLoading();

        if (!invalid)
            ProgressDialogManager.show(messageResId);

        return invalid;
    }

    private void logout() {
        if (invalidDataLoading(R.string.w_logout))
            return;

        AuthenticationCenter.getDefault().logout(new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                endDataLoading();

                if (error != null) {
                    AlertDialogUtils.retry(error.getMessage(), new Runnable() {
                        @Override
                        public void run() {
                            logout();
                        }
                    });
                }
            }
        });
    }

    private void openConnectedAccountsAlertDialog() {
        final ConnectedAccountsAlertDialogView view = new ConnectedAccountsAlertDialogView(invoker);

        new AlertDialog.Builder(invoker)
                .setTitle(R.string.w_connected_accounts)
                .setView(view)
                .setNegativeButton(R.string.w_dismiss, null)
                .create()
                .show();
    }

    private void openPublicSettingsAlertDialog() {
        final PublicSettingsAlertDialogView view = new PublicSettingsAlertDialogView(invoker);

        new AlertDialog.Builder(invoker)
                .setTitle(R.string.w_public_settings)
                .setView(view)
                .setNegativeButton(R.string.w_cancel, null)
                .setPositiveButton(R.string.w_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showChangePermissionAlertDialog(view.getCheckedPermission());
                    }
                })
                .create()
                .show();
        view.setPermission(AuthenticationCenter.getDefault().getUser().user_options.album_permission);
    }

    private void openPushSettingsAlertDialog() {
        final PushSettingsAlertDialogView view = new PushSettingsAlertDialogView(invoker);

        new AlertDialog.Builder(invoker)
                .setTitle(R.string.w_push_notification_settings)
                .setView(view)
                .setNegativeButton(R.string.w_dismiss, null)
                .create()
                .show();
    }

    private void openVersionInfoAlertDialog() {
        final VersionInfoAlertDialogView view = new VersionInfoAlertDialogView(invoker);

        new AlertDialog.Builder(invoker)
                .setTitle(R.string.w_version_information)
                .setView(view)
                .setNegativeButton(R.string.w_dismiss, null)
                .create()
                .show();
    }

    private void openWebViewActivity(int pathResId, int titleResId) {
        String url = Resources.getString(R.string.domain) + Resources.getString(pathResId);
        Map<String, String> params = new HashMap<>();
        params.put("url", url);
        params.put("title", Resources.getString(titleResId));
        WebViewActivity.show(CustomSchemeGenerator.createWebLink(params));
    }

    private void showChangePermissionAlertDialog(final Album.Permission permission) {
        if (Album.Permission.Private.equals(permission)) {
            AlertDialogUtils.show(invoker.getString(R.string.m_warn_album_permission),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == AlertDialog.BUTTON_POSITIVE) {
                                changePermission(permission);
                            }
                        }
                    },
                    invoker.getString(R.string.w_cancel),
                    invoker.getString(R.string.w_change));
        } else {
            changePermission(permission);
        }
    }
}
