package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.itemview.ApplicationOptionsItemView;
import com.orcller.app.orcller.itemview.OptionsSectionHeaderView;
import com.orcller.app.orcller.proxy.OptionsActivityProxy;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.itemview.ListBadgeItemView;
import pisces.psuikit.itemview.ListBaseItemView;
import pisces.psuikit.manager.ProgressDialogManager;
import pisces.psuikit.widget.Badge;
import pisces.psuikit.widget.SectionedListView;

/**
 * Created by pisces on 12/23/15.
 */
public class OptionsActivity extends PSActionBarActivity implements SectionedListView.DataSource {
    private static final int APPLICATION_OPTIONS_ITEM_VIEW = 1;
    private static final int BASE_ITEM_VIEW = 2;
    private static final int BADGE_ITEM_VIEW = 3;
    private OptionsActivityProxy optionsActivityProxy;
    private SectionedListView listView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_options);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.w_title_options));

        listView = (SectionedListView) findViewById(R.id.listView);
        optionsActivityProxy = new OptionsActivityProxy(this);
        View footerView = new View(this);

        footerView.setBackgroundColor(getResources().getColor(R.color.background_album_view));
        footerView.setLayoutParams(new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, GraphicUtils.convertDpToPixel(45)));
        listView.addFooterView(footerView);
        listView.setDataSource(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ProgressDialogManager.hide();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public SectionedListView getListView() {
        return listView;
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * SectionedListView.DataSource
     */
    public View getHeaderView(ListView listView, View convertView, int section) {
        if (convertView == null)
            convertView = new OptionsSectionHeaderView(this);

        if (convertView instanceof OptionsSectionHeaderView)
            ((OptionsSectionHeaderView) convertView).setText(getHeaderTitleResId(section));

        return convertView;
    }

    public View getItemView(ListView listView, View convertView, SectionedListView.IndexPath indexPath) {
        if (convertView == null) {
            int type = getItemViewType(listView, indexPath);
            if (type == APPLICATION_OPTIONS_ITEM_VIEW)
                convertView = new ApplicationOptionsItemView(this);
            else if (type == BADGE_ITEM_VIEW)
                convertView = new ListBadgeItemView(this);
            else
                convertView = new ListBaseItemView(this);
        }

        if (convertView instanceof ListBaseItemView) {
            ListBaseItemView view = (ListBaseItemView) convertView;
            view.setTitleText(getItemTitleResId(indexPath));
            view.setDetailText(getItemDetailText(indexPath));

            if (indexPath.section() == 3 && indexPath.row() == 0)
                view.setTitleTextColor(Color.RED);
            else
                view.setTitleTextColor(Color.BLACK);

            if (convertView instanceof ListBadgeItemView) {
                Badge badge = ((ListBadgeItemView) convertView).getBadge();
                String text = Application.isLowerAppVersion(ApplicationLauncher.getDefault().getCurrentVersion()) ? "N" : null;
                badge.setText(text);
            }
        }

        return convertView;
    }

    public int getRowCount(ListView listView, int section) {
        if (section == 0)
            return 3;
        if (section == 1)
            return 2;
        if (section == 2)
            return 2;
        return 1;
    }

    public int getSectionCount(ListView listView) {
        return 4;
    }

    public int getItemViewType(ListView listView, SectionedListView.IndexPath indexPath) {
        if (indexPath.section() == 1) {
            if (indexPath.row() == 1)
                return BADGE_ITEM_VIEW;
        }
        return BASE_ITEM_VIEW;
    }

    public int getItemViewTypeCount(ListView listView) {
        return 2;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private int getHeaderTitleResId(int section) {
        if (section == 0)
            return R.string.w_account;
        if (section == 1)
            return R.string.w_settings;
        if (section == 2)
            return R.string.w_about;
        return 0;
    }

    private int getItemTitleResId(SectionedListView.IndexPath indexPath) {
        if (indexPath.section() == 0) {
            if (indexPath.row() == 0)
                return R.string.w_connected_accounts;
            if (indexPath.row() == 1)
                return R.string.w_change_password;
            return R.string.w_albums_public_settings;
        }

        if (indexPath.section() == 1) {
            if (indexPath.row() == 0)
                return R.string.w_push_notification_settings;
            return R.string.w_version_information;
        }

        if (indexPath.section() == 2) {
            if (indexPath.row() == 0)
                return R.string.w_private_policy;
            return R.string.w_terms;
        }

        if (indexPath.section() == 3)
            return R.string.w_log_out;
        return 0;
    }

    private String getItemDetailText(SectionedListView.IndexPath indexPath) {
        if (indexPath.section() == 0 && indexPath.row() == 2) {
            User user = AuthenticationCenter.getDefault().getUser();
            return user != null ? SharedObject.getPermissionText(user.user_options.album_permission) : null;
        }

        if (indexPath.section() == 1) {
            if (indexPath.row() == 0)
                return Application.isNotificationEnabled() ? "On" : "Off";
            return Application.getPackageVersionName();
        }

        return null;
    }
}
