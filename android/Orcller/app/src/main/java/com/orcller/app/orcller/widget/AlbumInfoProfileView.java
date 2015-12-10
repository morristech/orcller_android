package com.orcller.app.orcller.widget;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.AlbumInfo;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/3/15.
 */
public class AlbumInfoProfileView extends PSLinearLayout {
    private AlbumInfo model;
    private TextView idTextView;
    private TextView nameTextView;
    private TextView dateTextView;
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
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);

        idTextView.setMovementMethod(LinkMovementMethod.getInstance());
        EventBus.getDefault().register(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public AlbumInfo getModel() {
        return model;
    }

    public void setModel(AlbumInfo model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        idTextView.setText(CustomSchemeGenerator.createUserProfileHtml(model.getUser()));
        nameTextView.setText(model.user_name);
        dateTextView.setText(DateUtil.getRelativeTimeSpanString(model.updated_time));
        userPictureView.setModel(model.getUser());
    }

    public void reload() {
        idTextView.setText(CustomSchemeGenerator.createUserProfileHtml(model.getUser()));
        nameTextView.setText(model.user_name);
        userPictureView.reload();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (!model.isMine() || !(event instanceof AlbumInfo.Event))
            return;

        reload();
    }
}
