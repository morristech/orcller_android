package com.orcller.app.orcller.widget;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.FollowersActivity;
import com.orcller.app.orcller.activity.FollowingActivity;
import com.orcller.app.orcller.activity.UserPictureActivity;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;
import com.orcller.app.orcllermodules.utils.SoftKeyboardUtils;

import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;
import pisces.psuikit.widget.ClearableEditText;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/10/15.
 */
public class ProfileHearderView extends PSLinearLayout implements Validator.ValidationListener, View.OnClickListener {
    private boolean editing;
    private Validator validator;
    private Delegate delegate;
    private User model;

    @Length(max = 16, messageResId = R.string.m_validate_user_nickname_length)
    private ClearableEditText nickNameEditText;

    @Length(max = 50, messageResId = R.string.m_validate_profile_message_length)
    private ClearableEditText messageEditText;

    private LinearLayout buttonContainer;
    private View separator;
    private Button followersButton;
    private Button followingButton;
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

        nickNameEditText = (ClearableEditText) findViewById(R.id.nickNameEditText);
        messageEditText = (ClearableEditText) findViewById(R.id.messageEditText);
        separator = findViewById(R.id.separator);
        buttonContainer = (LinearLayout) findViewById(R.id.buttonContainer);
        followersButton = (Button) findViewById(R.id.followersButton);
        followingButton = (Button) findViewById(R.id.followingButton);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);
        validator = new Validator(this);

        followersButton.setOnClickListener(this);
        followingButton.setOnClickListener(this);
        userPictureView.setOnClickListener(this);
        validator.setValidationListener(this);
        EventBus.getDefault().register(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isEditing() {
        return editing;
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public User getModel() {
        return model;
    }

    public void setModel(User model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    public void save() {
        validator.validate();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * OnClick listener
     */
    public void onClick(View v) {
        if (followingButton.equals(v)) {
            FollowingActivity.show(model.user_uid);
        } else if (followersButton.equals(v)) {
            FollowersActivity.show(model.user_uid);
        } else if (userPictureView.equals(v)) {
            UserPictureActivity.show(model);
        }
    }

    /**
     * EventBus listener
     */
    public void onEventMainThread(final Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                editing = true;

                if (nickNameEditText.hasFocus())
                    nickNameEditText.setCursorVisible(true);

                if (messageEditText.hasFocus())
                    messageEditText.setCursorVisible(true);

                buttonContainer.setVisibility(GONE);
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                editing = false;

                modelChanged();
                nickNameEditText.setCursorVisible(false);
                nickNameEditText.clearFocus();
                messageEditText.setCursorVisible(false);
                messageEditText.clearFocus();
                buttonContainer.setVisibility(VISIBLE);
            }

            if (delegate != null)
                delegate.onChangeState();
        } else if (event instanceof Model.Event) {
            Model.Event casted = (Model.Event) event;
            if (Model.Event.SYNCHRONIZE.equals(casted.getType()) &&
                    casted.getTarget() instanceof User) {
                synchronizeModel((User) casted.getTarget());
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
        if (invalidDataLoading())
            return;

        try {
            final ProgressDialog progressDialog = ProgressDialog.show(
                    Application.getTopActivity(), null, Application.applicationContext().getString(R.string.w_saving));
            final Runnable error = new Runnable() {
                @Override
                public void run() {
                    progressDialog.hide();
                    endDataLoading();

                    AlertDialogUtils.retry(R.string.m_fail_change, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == AlertDialog.BUTTON_POSITIVE) {
                                onValidationSucceeded();
                            } else {
                                modelChanged();
                                SoftKeyboardUtils.hide(nickNameEditText);
                            }
                        }
                    });
                }
            };

            final User clonedUser = (User) model.clone();
            clonedUser.user_name = nickNameEditText.getText().toString().trim();
            clonedUser.user_profile_message = messageEditText.getText().toString().trim();

            UserDataProxy.getDefault().saveProfile(clonedUser, new Callback<ApiResult>() {
                @Override
                public void onResponse(Response<ApiResult> response, Retrofit retrofit) {
                    if (response.isSuccess() && response.body().isSuccess()) {
                        progressDialog.hide();
                        endDataLoading();
                        AuthenticationCenter.getDefault().synchorinzeUser(clonedUser, new Runnable() {
                            @Override
                            public void run() {
                                SoftKeyboardUtils.hide(nickNameEditText);
                            }
                        });
                    } else {
                        if (DEBUG)
                            e("Api Error", response.body());

                        error.run();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    if (BuildConfig.DEBUG)
                        Log.e("onFailure", t);

                    error.run();
                }
            });
        } catch (CloneNotSupportedException e) {
            if (BuildConfig.DEBUG)
                Log.e(e.getMessage(), e);
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private String getCountText(int count) {
        return count > 0 ? " " + String.valueOf(count) : "";
    }

    private void modelChanged() {
        userPictureView.setModel(model);
        nickNameEditText.setHint(model.isMe() ? R.string.m_hint_user_name_me : R.string.m_hint_user_name);
        nickNameEditText.setText(model.user_name);
        nickNameEditText.setEnabled(model.isMe());
        messageEditText.setHint(model.isMe() ? R.string.m_hint_profile_message_me : R.string.m_hint_profile_message);
        messageEditText.setText(model.user_profile_message);
        messageEditText.setEnabled(model.isMe());
        followersButton.setText(getContext().getString(R.string.w_followers) + getCountText(model.user_options.follower_count));
        followingButton.setText(getContext().getString(R.string.w_following) + getCountText(model.user_options.follow_count));

        if (!model.isMe()) {
            nickNameEditText.getLayoutParams().height = LayoutParams.WRAP_CONTENT;

            LayoutParams params = (LayoutParams) messageEditText.getLayoutParams();
            params.height = LayoutParams.WRAP_CONTENT;
            params.topMargin = 0;

            nickNameEditText.setBackground(null);
            nickNameEditText.setPadding(nickNameEditText.getPaddingLeft(), 0, 0, GraphicUtils.convertDpToPixel(8));
            messageEditText.setBackground(null);
            messageEditText.setPadding(messageEditText.getPaddingLeft(), GraphicUtils.convertDpToPixel(8), 0, 0);
            separator.setVisibility(VISIBLE);
        }
    }

    private void synchronizeModel(User user) {
        if (user.equals(model)) {
            userPictureView.reload();
        } else if (user.user_uid == model.user_uid) {
            model.synchronize(user, new Runnable() {
                @Override
                public void run() {
                    modelChanged();

                    if (delegate != null)
                        delegate.onSyncModel();
                }
            });
        }
    }

    // ================================================================================================
    //  Delegate
    // ================================================================================================

    public interface Delegate {
        void onChangeState();
        void onSyncModel();
    }
}