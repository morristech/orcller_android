package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Album;

import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/8/15.
 */
public class PublicSettingsAlertDialogView extends PSLinearLayout {
    private Album model;
    private RadioGroup radioGroup;

    public PublicSettingsAlertDialogView(Context context) {
        super(context);
    }

    public PublicSettingsAlertDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PublicSettingsAlertDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSScrollView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_alertdialog_public_settings, this);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Album getModel() {
        return model;
    }

    public void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;
        RadioButton radioButton = (RadioButton) radioGroup.getChildAt(model.permission - 1);
        radioButton.setChecked(true);
    }

    public Album.Permission getCheckedPermission() {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.publicRadioButton:
                return Album.Permission.Public;

            case R.id.followersRadioButton:
                return Album.Permission.Followers;

            case R.id.privateRadioButton:
                return Album.Permission.Private;
        }
        return Album.Permission.None;
    }
}
