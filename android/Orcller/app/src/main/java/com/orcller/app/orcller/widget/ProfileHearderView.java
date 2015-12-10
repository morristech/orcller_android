package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.model.User;

import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;
import pisces.psuikit.widget.ClearableEditText;

/**
 * Created by pisces on 12/10/15.
 */
public class ProfileHearderView extends PSLinearLayout implements Validator.ValidationListener, View.OnClickListener {
    private Validator validator;
    private User model;
    private ClearableEditText nameEditText;
    private ClearableEditText messageEditText;
    private Button followersButton;
    private Button followingButton;
    private Button saveButton;
    private UserPictureView userPictureView;

    public ProfileHearderView(Context context) {
        super(context);
    }

    public ProfileHearderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProfileHearderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_profile_header, this);

        nameEditText = (ClearableEditText) findViewById(R.id.nameEditText);
        messageEditText = (ClearableEditText) findViewById(R.id.messageEditText);
        followersButton = (Button) findViewById(R.id.followersButton);
        followingButton = (Button) findViewById(R.id.followingButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);
        validator = new Validator(this);

        followersButton.setOnClickListener(this);
        followingButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        validator.setValidationListener(this);
        EventBus.getDefault().register(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public User getModel() {
        return model;
    }

    public void setModel(User model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        nameEditText.setText(model.user_name);
        nameEditText.setEnabled(model.isMe());
        messageEditText.setText(model.user_profile_message);
        messageEditText.setEnabled(model.isMe());
        userPictureView.setModel(model);
        followersButton.setText(getContext().getString(R.string.w_followers) + getCountText(model.user_options.follower_count));
        followingButton.setText(getContext().getString(R.string.w_following) + getCountText(model.user_options.follow_count));
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * OnClick listener
     */
    public void onClick(View v) {
        if (saveButton.equals(v)) {
            validator.validate();
        } else if (followingButton.equals(v)) {
            //TODO: open following list
        } else if (followersButton.equals(v)) {
            //TODO: open follower list
        }
        Log.d("v", v);
    }

    /**
     * EventBus listener
     */
    public void onEventMainThread(final Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                if (nameEditText.hasFocus())
                    nameEditText.setCursorVisible(true);

                if (messageEditText.hasFocus())
                    messageEditText.setCursorVisible(true);

                saveButton.setVisibility(VISIBLE);
                followersButton.setVisibility(GONE);
                followingButton.setVisibility(GONE);
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                nameEditText.setCursorVisible(false);
                messageEditText.setCursorVisible(false);
                saveButton.setVisibility(GONE);
                followersButton.setVisibility(VISIBLE);
                followingButton.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * Validator listener
     */
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getContext());
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onValidationSucceeded() {
        save();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private String getCountText(int count) {
        return count > 0 ? " " + String.valueOf(count) : "";
    }

    private void save() {

    }
}