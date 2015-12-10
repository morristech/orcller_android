package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Comment;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;
import com.orcller.app.orcller.widget.UserPictureView;

import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/5/15.
 */
public class CommentItemView extends PSLinearLayout implements View.OnClickListener {
    private Comment model;
    private Delegate delegate;
    private View separator;
    private TextView idTextView;
    private TextView commentTextView;
    private TextView dateTextView;
    private UserPictureView userPictureView;
    private ImageView deleteImageView;

    public CommentItemView(Context context) {
        super(context);
    }

    public CommentItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_comment, this);

        idTextView = (TextView) findViewById(R.id.idTextView);
        commentTextView = (TextView) findViewById(R.id.commentTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);
        deleteImageView = (ImageView) findViewById(R.id.deleteImageView);
        separator = findViewById(R.id.separator);

        idTextView.setMovementMethod(LinkMovementMethod.getInstance());
        deleteImageView.setOnClickListener(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public Comment getModel() {
        return model;
    }

    public void setModel(Comment model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    public void setSeparatorVisibility(boolean visible) {
        separator.setVisibility(visible ? VISIBLE : GONE);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onClick(View v) {
        if (delegate != null)
            delegate.onClickDeleteButton(this);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void modelChanged() {
        idTextView.setText(CustomSchemeGenerator.createUserProfileHtml(model));
        commentTextView.setText(model.message);
        dateTextView.setText(DateUtil.getRelativeTimeSpanString(model.created_time));
        userPictureView.setModel(model);
        deleteImageView.setVisibility(model.isMe() ? VISIBLE : GONE);
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public static interface Delegate {
        void onClickDeleteButton(CommentItemView target);
    }
}
