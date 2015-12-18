package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Contributor;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;
import com.orcller.app.orcller.widget.CoeditButton;
import com.orcller.app.orcller.widget.FollowButton;
import com.orcller.app.orcller.widget.UserPictureView;

import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/14/15.
 */
public class ContributorItemView  extends PSLinearLayout {
    private boolean allowsShowCoeditButton = true;
    private long albumId;
    private Contributor model;
    private View separator;
    private TextView headerTextView;
    private TextView idTextView;
    private TextView nameTextView;
    private UserPictureView userPictureView;
    private FollowButton followButton;
    private CoeditButton coeditButton;

    public ContributorItemView(Context context) {
        super(context);
    }

    public ContributorItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContributorItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_contributor, this);

        headerTextView = (TextView) findViewById(R.id.textView);
        idTextView = (TextView) findViewById(R.id.idTextView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);
        followButton = (FollowButton) findViewById(R.id.followButton);
        coeditButton = (CoeditButton) findViewById(R.id.coeditButton);
        separator = findViewById(R.id.separator);

        idTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isAllowsShowCoeditButton() {
        return allowsShowCoeditButton;
    }

    public void setAllowsShowCoeditButton(boolean allowsShowCoeditButton) {
        if (allowsShowCoeditButton == this.allowsShowCoeditButton)
            return;

        this.allowsShowCoeditButton = allowsShowCoeditButton;
        coeditButton.setVisibility(allowsShowCoeditButton ? VISIBLE : GONE);
    }

    public CoeditButton.Delegate getDelegate() {
        return coeditButton.getDelegate();
    }

    public void setDelegate(CoeditButton.Delegate delegate) {
        coeditButton.setDelegate(delegate);
    }

    public CharSequence getHeaderText() {
        return headerTextView.getText();
    }

    public void setHeaderText(String text) {
        headerTextView.setText(text);
        headerTextView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setHeaderText(int resId) {
        setHeaderText(getContext().getString(resId));
    }

    public Contributor getModel() {
        return model;
    }

    public void setModel(Contributor model, long albumId) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;
        this.albumId = albumId;

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

        try {
            coeditButton.setModel(model, albumId);
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.d(e.getMessage(), e);
        }

        coeditButton.setVisibility(allowsShowCoeditButton ? coeditButton.getVisibility() : GONE);
        followButton.setModel(model);

        if (model.isFollowing() || coeditButton.getVisibility() == VISIBLE)
            followButton.setVisibility(GONE);
    }
}
