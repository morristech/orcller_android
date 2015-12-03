package com.orcller.app.orcller.widget;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Page;

import java.util.Date;

import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/3/15.
 */
public class PageProfileView extends PSLinearLayout {
    private Page model;
    private TextView idTextView;
    private TextView nameTextView;
    private TextView dateTextView;
    private UserPictureView userPictureView;

    public PageProfileView(Context context) {
        super(context);
    }

    public PageProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageProfileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_pageprofile, this);

        idTextView = (TextView) findViewById(R.id.idTextView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Page getModel() {
        return model;
    }

    public void setModel(Page model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        idTextView.setText(model.user_id);
        nameTextView.setText(model.user_name);
        dateTextView.setText(DateUtil.getRelativeTimeSpanString(model.updated_time));
        userPictureView.setModel(model.getUser());
    }
}
