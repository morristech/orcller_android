package com.orcller.app.orcller.widget;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.AlbumInfo;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/3/15.
 */
public class AlbumInfoProfileView extends PSLinearLayout {
    public static final int ALBUM_NAME = 1;
    public static final int USER_NICKNAME = 2;
    private int descriptionMode = USER_NICKNAME;
    private AlbumInfo model;
    private TextView idTextView;
    private TextView nameTextView;
    private TextView dateTextView;
    private ImageView optionsIcon;
    private UserPictureView userPictureView;

    public AlbumInfoProfileView(Context context) {
        super(context);
    }

    public AlbumInfoProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumInfoProfileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_album_info_profile, this);

        idTextView = (TextView) findViewById(R.id.idTextView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        optionsIcon = (ImageView) findViewById(R.id.optionsIcon);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);

        idTextView.setMovementMethod(LinkMovementMethod.getInstance());
        EventBus.getDefault().register(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public int getDescriptionMode() {
        return descriptionMode;
    }

    public void setDescriptionMode(int descriptionMode) {
        this.descriptionMode = descriptionMode;

        if (model != null)
            updateDescription();
    }

    public AlbumInfo getModel() {
        return model;
    }

    public void setModel(AlbumInfo model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        idTextView.setText(CustomSchemeGenerator.createUserProfileHtml(model.getUser()));
        dateTextView.setText(DateUtil.getRelativeTimeSpanString(model.updated_time));
        userPictureView.setModel(model.getUser());
        updateDescription();
    }

    public ImageView getOptionsIcon() {
        return optionsIcon;
    }

    public void reload() {
        idTextView.setText(CustomSchemeGenerator.createUserProfileHtml(model.getUser()));
        dateTextView.setText(DateUtil.getRelativeTimeSpanString(model.updated_time));
        userPictureView.reload();
        updateDescription();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (!model.isMine() || !(event instanceof AlbumInfo.Event))
            return;

        reload();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void updateDescription() {
        if (getDescriptionMode() == ALBUM_NAME && model instanceof Album) {
            nameTextView.setText(((Album) model).name);
        } else if (getDescriptionMode() == USER_NICKNAME) {
            nameTextView.setText(model.user_name);
        }

        nameTextView.setVisibility(TextUtils.isEmpty(nameTextView.getText()) ? GONE : VISIBLE);
    }
}
