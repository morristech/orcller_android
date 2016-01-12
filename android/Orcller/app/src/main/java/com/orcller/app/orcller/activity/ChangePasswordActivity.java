package com.orcller.app.orcller.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.ChangePasswordListItemView;
import com.orcller.app.orcller.itemview.OptionsSectionHeaderView;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import pisces.psuikit.utils.AlertDialogUtils;

import pisces.psfoundation.ext.Application;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressDialogManager;
import pisces.psuikit.widget.SectionedListView;

/**
 * Created by pisces on 12/24/15.
 */
public class ChangePasswordActivity extends PSActionBarActivity implements SectionedListView.DataSource {
    private SectionedListView listView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.w_change_password));

        listView = (SectionedListView) findViewById(R.id.listView);

        listView.setDataSource(this);
        listView.reload();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Application.getTopActivity().getMenuInflater().inflate(R.menu.menu_change, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change:
                validateItemViews();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        endDataLoading();
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressDialogManager.hide();
    }

    @Override
    public boolean invalidDataLoading() {
        boolean invalid = super.invalidDataLoading();

        if (!invalid)
            ProgressDialogManager.show(R.string.w_change_password_ing);

        return invalid;
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

        if (convertView instanceof OptionsSectionHeaderView) {
            OptionsSectionHeaderView view = (OptionsSectionHeaderView) convertView;
            view.setText(null);
        }

        return convertView;
    }

    public View getItemView(ListView listView, View convertView, SectionedListView.IndexPath indexPath) {
        if (convertView == null) {
            convertView = new ChangePasswordListItemView(this);
        }

        if (convertView instanceof ChangePasswordListItemView) {
            ChangePasswordListItemView view = (ChangePasswordListItemView) convertView;
            view.setHint(getHintResId(indexPath));
        }

        return convertView;
    }

    public int getRowCount(ListView listView, int section) {
        if (section == 0)
            return 1;
        return 2;
    }

    public int getSectionCount(ListView listView) {
        return 2;
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

    private int getHintResId(SectionedListView.IndexPath indexPath) {
        if (indexPath.section() == 0)
            return R.string.m_hint_current_password;
        return indexPath.row() == 0 ? R.string.m_hint_new_password : R.string.m_hint_new_password_again;
    }

    private void changePassword(final String currentPassword, final String newPassword) {
        if (invalidDataLoading())
            return;

        ApiMember.ChangePasswordReq req = new ApiMember.ChangePasswordReq();
        req.current_password = currentPassword;
        req.change_password = newPassword;

        AuthenticationCenter.getDefault().changePassword(req, new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                if (error == null) {
                    AlertDialogUtils.show(R.string.m_complete_change, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, R.string.w_ok);
                } else {
                    AlertDialogUtils.retry(R.string.m_fail_change, new Runnable() {
                        @Override
                        public void run() {
                            changePassword(currentPassword, newPassword);
                        }
                    });
                }
            }
        });
    }

    private void validateItemViews() {
        ChangePasswordListItemView currentItem = (ChangePasswordListItemView) listView.getChildAt(1);
        ChangePasswordListItemView newItem = (ChangePasswordListItemView) listView.getChildAt(3);
        ChangePasswordListItemView againItem = (ChangePasswordListItemView) listView.getChildAt(4);

        currentItem.validate();
        newItem.validate();
        againItem.validate();

        if (currentItem.isValid() && newItem.isValid() && againItem.isValid()) {
            if (currentItem.getTitleText().equals(newItem.getTitleText())) {
                newItem.getEditText().setError(getString(R.string.m_validate_change_password_new));
                newItem.getEditText().requestFocus();
            } else if (!newItem.getTitleText().equals(againItem.getTitleText())) {
                againItem.getEditText().setError(getString(R.string.m_validate_change_password_again));
                againItem.getEditText().requestFocus();
            } else {
                changePassword(currentItem.getTitleText(), newItem.getTitleText());
            }
        }
    }
}
