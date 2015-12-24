package com.orcller.app.orcller.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;

import pisces.psfoundation.ext.Application;
import pisces.psuikit.ext.PSFrameLayout;
import pisces.psuikit.itemview.ListBaseItemView;
import pisces.psuikit.itemview.ListEmptyHeaderView;
import pisces.psuikit.widget.SectionedListView;

/**
 * Created by pisces on 12/24/15.
 */
public class VersionInfoAlertDialogView extends PSFrameLayout
        implements SectionedListView.DataSource, SectionedListView.Delegate {
    private SectionedListView listView;

    public VersionInfoAlertDialogView(Context context) {
        super(context);
    }

    public VersionInfoAlertDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VersionInfoAlertDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_alertdialog_sectioned_list, this);

        listView = (SectionedListView) findViewById(R.id.listView);

        listView.setDataSource(this);
        listView.setDelegate(this);
        listView.reload();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * SectionedListView.DataSource
     */
    public View getHeaderView(ListView listView, View convertView, int section) {
        if (convertView == null)
            convertView = new ListEmptyHeaderView(getContext());
        return convertView;
    }

    public View getItemView(ListView listView, View convertView, SectionedListView.IndexPath indexPath) {
        if (convertView == null) {
            convertView = new ListBaseItemView(getContext());
        }

        if (convertView instanceof ListBaseItemView) {
            ListBaseItemView view = (ListBaseItemView) convertView;
            int resId = indexPath.row() == 1 && ApplicationLauncher.getDefault().hasNewVersion() ?
                    R.color.theme_purple_accent : R.color.itemview_list_base_detail_text;

            view.setDetailText(getDescriptionText(indexPath));
            view.setDetailTextColor(getResources().getColor(resId));
            view.setTitleText(getTitleTextResId(indexPath));
        }

        return convertView;
    }

    public int getRowCount(ListView listView, int section) {
        return 2;
    }

    public int getSectionCount(ListView listView) {
        return 1;
    }

    public int getItemViewType(ListView listView, SectionedListView.IndexPath indexPath) {
        return 1;
    }

    public int getItemViewTypeCount(ListView listView) {
        return 1;
    }

    /**
     * SectionedListView.Delegate
     */
    public boolean isEnabled(ListView listView, SectionedListView.IndexPath indexPath) {
        return indexPath.row() == 1 && ApplicationLauncher.getDefault().hasNewVersion();
    }

    public void onItemClick(ListView listView, View view, SectionedListView.IndexPath indexPath) {
        ApplicationLauncher.getDefault().openPlayStore();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private String getDescriptionText(SectionedListView.IndexPath indexPath) {
        return indexPath.row() == 0 ? Application.getPackageVersionName() : ApplicationLauncher.getDefault().getCurrentVersion();
    }

    private int getTitleTextResId(SectionedListView.IndexPath indexPath) {
        return indexPath.row() == 0 ? R.string.w_current_version : R.string.w_lastest_version;
    }
}
