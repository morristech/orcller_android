package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;
import com.orcller.app.orcller.widget.FollowButton;
import com.orcller.app.orcller.widget.UserPictureView;
import com.orcller.app.orcllermodules.model.BaseUser;

import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/10/15.
 */
public class UserItemView extends PSLinearLayout {
    private boolean allowsShowFollowButton;
    private BaseUser model;
    private View separator;
    private TextView idTextView;
    private TextView nameTextView;
    private UserPictureView userPictureView;
    private FollowButton followButton;

    public UserItemView(Context context) {
        super(context);
    }

    public UserItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_user, this);

        idTextView = (TextView) findViewById(R.id.idTextView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);
        followButton = (FollowButton) findViewById(R.id.followButton);
        separator = findViewById(R.id.separator);

        idTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isAllowsShowFollowButton() {
        return allowsShowFollowButton;
    }

    public void setAllowsShowFollowButton(boolean allowsShowFollowButton) {
        if (allowsShowFollowButton == this.allowsShowFollowButton)
            return;

        this.allowsShowFollowButton = allowsShowFollowButton;
    }

    public BaseUser getModel() {
        return model;
    }

    public void setModel(BaseUser model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    public void setSeparatorVisibility(boolean visible) {
        separator.setVisibility(visible ? VISIBLE : GONE);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void modelChanged() {
        userPictureView.setModel(model);
        idTextView.setText(CustomSchemeGenerator.createUserProfileHtml(model));
        nameTextView.setText(model.user_name);
        nameTextView.setVisibility(TextUtils.isEmpty(model.user_name) ? GONE : VISIBLE);
        followButton.setModel(model);
        followButton.setVisibility(allowsShowFollowButton ? followButton.getVisibility() : GONE);
    }
}
