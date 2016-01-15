package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.Notification;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;
import com.orcller.app.orcller.widget.FollowButton;
import com.orcller.app.orcller.widget.UserPictureView;
import com.orcller.app.orcllermodules.model.BaseUser;
import com.orcller.app.orcllermodules.model.User;

import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSImageView;
import pisces.psuikit.ext.PSLinearLayout;
import pisces.psuikit.ext.PSView;

/**
 * Created by pisces on 12/18/15.
 */
public class ActivityItemView extends PSLinearLayout implements FollowButton.Delegate {
    private Notification model;
    private View separator;
    private TextView contentTextView;
    private TextView dateTextView;
    private PSImageView contentImageView;
    private UserPictureView userPictureView;
    private FollowButton followButton;

    public ActivityItemView(Context context) {
        super(context);
    }

    public ActivityItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActivityItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_activity, this);

        contentTextView = (TextView) findViewById(R.id.contentTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        contentImageView = (PSImageView) findViewById(R.id.contentImageView);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);
        followButton = (FollowButton) findViewById(R.id.followButton);
        separator = findViewById(R.id.separator);

        followButton.setDelegate(this);
        contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Notification getModel() {
        return model;
    }

    public void setModel(Notification model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    public void setSeparatorVisibility(boolean visible) {
        separator.setVisibility(visible ? VISIBLE : GONE);
    }

    // ================================================================================================
    //  Protocol Implementation
    // ================================================================================================

    public void onCompleteFollow(FollowButton target, BaseUser user) {
        followButton.setVisibility(GONE);
    }

    public void onCompleteUnfollow(FollowButton target, BaseUser user) {
        followButton.setVisibility(VISIBLE);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private int getContentResId() {
        if (Notification.Type.AlbumCreation.equals(model.type))
            return R.string.m_noty_album_create;
        if (Notification.Type.AlbumModification.equals(model.type))
            return R.string.m_noty_album_modify;
        if (Notification.Type.CoeditingAccept.equals(model.type))
            return User.isMe(model.content.content_user_uid) ?
                    R.string.m_noty_collaboration_accept_invite :
                    R.string.m_noty_collaboration_accept_ask;
        if (Notification.Type.CoeditingAsk.equals(model.type))
            return R.string.m_noty_collaboration_ask;
        if (Notification.Type.CoeditingInvite.equals(model.type))
            return R.string.m_noty_collaboration_invite;
        if (Notification.Type.Comment.equals(model.type))
            return R.string.m_noty_comment;
        if (Notification.Type.Favorite.equals(model.type))
            return R.string.m_noty_star;
        if (Notification.Type.Follow.equals(model.type))
            return R.string.m_noty_start_follow;
        if (Notification.Type.Like.equals(model.type))
            return R.string.m_noty_heart;
        if (Notification.Type.PageComment.equals(model.type))
            return R.string.m_noty_page_comment;
        if (Notification.Type.PageLike.equals(model.type))
            return R.string.m_noty_page_heart;
        if (Notification.Type.CoeditingCancelAsk.equals(model.type))
            return R.string.m_noty_collaboration_cancel_ask;
        if (Notification.Type.CoeditingCancelInvite.equals(model.type))
            return R.string.m_noty_collaboration_cancel_invite;
        return 0;
    }

    private boolean isDuplicatedType() {
        return Notification.Type.Comment.equals(model.type) ||
                Notification.Type.Favorite.equals(model.type) ||
                Notification.Type.Like.equals(model.type) ||
                Notification.Type.PageComment.equals(model.type) ||
                Notification.Type.PageLike.equals(model.type);
    }

    private void loadContentImage() {
        if (PSView.isShown(contentTextView) && model.content != null) {
            Glide.with(getContext())
                    .load(SharedObject.toFullMediaUrl(model.content.content_thumbnail_url))
                    .into(contentImageView);
        }
    }

    private void modelChanged() {
        String senders = isDuplicatedType() ?
                CustomSchemeGenerator.createNotificationSenders(model.senders.data, 3) :
                CustomSchemeGenerator.createLinkTag(model.senders.getFirstUser());
        String content = senders + Resources.getString(R.string.w_user_honorific) + Resources.getString(getContentResId());

        Glide.clear(contentImageView);
        userPictureView.setModel(model.senders.getFirstUser());
        contentTextView.setText(CustomSchemeGenerator.createSpannable(content));
        dateTextView.setText(DateUtil.getRelativeTimeSpanString(model.created_time));
        followButton.setModel(model.senders.getFirstUser());
        followButton.setVisibility(Notification.Type.Follow.equals(model.type) && !model.senders.getFirstUser().isFollowing() ? VISIBLE : GONE);
        contentImageView.setVisibility(PSView.isShown(followButton) ? GONE : VISIBLE);
        loadContentImage();
    }
}