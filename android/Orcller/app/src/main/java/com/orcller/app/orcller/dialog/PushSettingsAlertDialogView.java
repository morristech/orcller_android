package com.orcller.app.orcller.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.OptionsSectionHeaderView;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.api.ApiMember;

import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSFrameLayout;
import pisces.psuikit.itemview.ListSwitchItemView;
import pisces.psuikit.widget.SectionedListView;

/**
 * Created by pisces on 12/24/15.
 */
public class PushSettingsAlertDialogView extends PSFrameLayout
        implements SectionedListView.DataSource, View.OnClickListener {
    private SectionedListView listView;

    public PushSettingsAlertDialogView(Context context) {
        super(context);
    }

    public PushSettingsAlertDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PushSettingsAlertDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * View.OnClickListener
     */
    public void onClick(View v) {
        save();
    }

    /**
     * SectionedListView.DataSource
     */
    public View getHeaderView(ListView listView, View convertView, int section) {
        if (convertView == null)
            convertView = new OptionsSectionHeaderView(getContext());

        if (convertView instanceof OptionsSectionHeaderView) {
            OptionsSectionHeaderView view = (OptionsSectionHeaderView) convertView;
            view.setText(null);
        }

        return convertView;
    }

    public View getItemView(ListView listView, View convertView, SectionedListView.IndexPath indexPath) {
        if (convertView == null) {
            convertView = new ListSwitchItemView(getContext());
        }

        if (convertView instanceof ListSwitchItemView) {
            ListSwitchItemView view = (ListSwitchItemView) convertView;

            view.setTitleText(getTitleTextResId(indexPath));
            view.getSwitch().setChecked(isSwitchChecked(indexPath));
            view.getSwitch().setOnClickListener(this);
        }

        return convertView;
    }

    public int getRowCount(ListView listView, int section) {
        return 1;
    }

    public int getSectionCount(ListView listView) {
        return 3;
    }

    public int getItemViewType(ListView listView, SectionedListView.IndexPath indexPath) {
        return 1;
    }

    public int getItemViewTypeCount(ListView listView) {
        return 1;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private int getCheckedPnsType(int section) {
        return getItemView(section).getSwitch().isChecked() ? getPnsType(section) : 0;
    }

    private ListSwitchItemView getItemView(int section) {
        return (ListSwitchItemView) listView.getChildAt(getPosition(section));
    }

    private int getPnsType(int section) {
        return 1 << section;
    }

    private int getPosition(int section) {
        return 1 + (2 * section);
    }

    private int getTitleTextResId(SectionedListView.IndexPath indexPath) {
        if (indexPath.section() == 0)
            return R.string.w_notification_application;
        if (indexPath.section() == 1)
            return R.string.w_notification_album;
        return R.string.w_notification_relationships;
    }

    private boolean isSwitchChecked(SectionedListView.IndexPath indexPath) {
        int types = AuthenticationCenter.getDefault().getUser().user_options.pns_types;
        int type = getPnsType(indexPath.section());
        return (types & type) == type;
    }

    private void save() {
        if (invalidDataLoading())
            return;

        int pnsTypes = 0;
        for (int i=0; i<getSectionCount(listView); i++) {
            pnsTypes |= getCheckedPnsType(i);
        }

        final User user = AuthenticationCenter.getDefault().getUser();
        ApiMember.UpdateUserOptionsReq req = new ApiMember.UpdateUserOptionsReq();
        req.user_options_album_permission = user.user_options.album_permission;
        req.user_options_pns_types = pnsTypes;

        AuthenticationCenter.getDefault().updateUserOptions(req, new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                listView.reload();
                endDataLoading();
            }
        });
    }
}
