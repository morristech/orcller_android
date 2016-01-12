package com.orcller.app.orcller.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.error.APIError;
import pisces.psuikit.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import com.orcller.app.orcllermodules.model.facebook.FBUser;
import pisces.psuikit.utils.AlertDialogUtils;
import pisces.psuikit.keyboard.SoftKeyboardNotifier;
import pisces.psuikit.keyboard.SoftKeyboardUtils;

import java.io.Serializable;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressDialogManager;
import pisces.psuikit.widget.ClearableEditText;

/**
 * Created by pisces on 11/12/15.
 */
public class MemberJoinInputActivity extends PSActionBarActivity implements Validator.ValidationListener {
    private static final String EMAIL_KEY = "email";
    private static final String USER_KEY = "user";

    @Length(min = 6, max = 16, messageResId = R.string.m_validate_user_name_length)
    private ClearableEditText idEditText;

    @Length(min = 6, max = 16, messageResId = R.string.m_validate_password_length)
    private ClearableEditText pwEditText;

    private String email;
    private LinearLayout container;
    private Button joinButton;
    private TextView extraTextView;
    private TextView descTextView;
    private TextWatcher textWatcher;
    private Validator validator;
    private FBUser user;

    // ================================================================================================
    //  Overridden: Activity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_memeber_joininput);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getResources().getString(R.string.w_sign_up));

        container = (LinearLayout) findViewById(R.id.container);
        joinButton = (Button) findViewById(R.id.joinButton);
        descTextView = (TextView) findViewById(R.id.descTextView);
        extraTextView = (TextView) findViewById(R.id.extraTextView);
        idEditText = (ClearableEditText) findViewById(R.id.idEditText);
        pwEditText = (ClearableEditText) findViewById(R.id.pwEditText);
        validator = new Validator(this);
        textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmptyId = TextUtils.isEmpty(idEditText.getText().toString().trim());
                boolean isEmptyPw = TextUtils.isEmpty(pwEditText.getText().toString().trim());
                joinButton.setVisibility(isEmptyId || isEmptyPw ? View.GONE : View.VISIBLE);
            }
        };

        descTextView.setMovementMethod(LinkMovementMethod.getInstance());
        loadExtra();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SoftKeyboardNotifier.getDefault().unregister(this);
        idEditText.setOnFocusChangeListener(null);
        pwEditText.setOnFocusChangeListener(null);
        pwEditText.setOnKeyListener(null);
        joinButton.setOnClickListener(null);
        container.setOnTouchListener(null);
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressDialogManager.hide();
    }

    @Override
    public boolean invalidDataLoading() {
        boolean result = super.invalidDataLoading();

        if (!result)
            ProgressDialogManager.show(R.string.w_processing);

        return result;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                if (idEditText.hasFocus())
                    idEditText.setCursorVisible(true);

                if (pwEditText.hasFocus())
                    pwEditText.setCursorVisible(true);
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                idEditText.setCursorVisible(false);
                pwEditText.setCursorVisible(false);
            }
        }
    }

    public void onValidationSucceeded() {
        join();
    }

    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getBaseContext());
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void loadExtra() {
        setUser(getIntent().getSerializableExtra(USER_KEY));

        if (getIntent().getStringExtra(EMAIL_KEY) != null) {
            setEmail(getIntent().getStringExtra(EMAIL_KEY));
        } else if (getIntent().getData() != null && getIntent().getData().getQueryParameter(EMAIL_KEY) != null) {
            setEmail(getIntent().getData().getQueryParameter(EMAIL_KEY));
        }
    }

    private void join() {
        SoftKeyboardUtils.hide(container);

        if (user != null) {
            joinWithIdp();
        } else if (email != null) {
            joinWithEmail();
        }
    }

    private void joinWithEmail() {
        if (invalidDataLoading())
            return;

        AuthenticationCenter.getDefault().join(getJoinWithEmailReq(), new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, final APIError error) {
                endDataLoading();

                if (error != null) {
                    String message = error.getMessage();
                    if (message != null) {
                        AlertDialogUtils.retry(message, new Runnable() {
                            @Override
                            public void run() {
                                joinWithEmail();
                            }
                        });
                    }
                }
            }
        });
    }

    private void joinWithIdp() {
        if (invalidDataLoading())
            return;

        AuthenticationCenter.getDefault().join(getJoinWithIdpReq(), new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, final APIError error) {
                endDataLoading();

                if (error != null) {
                    String message = error.getMessage();
                    if (message != null) {
                        AlertDialogUtils.retry(message, new Runnable() {
                            @Override
                            public void run() {
                                joinWithIdp();
                            }
                        });
                    }
                }
            }
        });
    }

    private void setListeners() {
        idEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                idEditText.setCursorVisible(hasFocus);
            }
        });
        pwEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                pwEditText.setCursorVisible(hasFocus);
            }
        });
        pwEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == event.KEYCODE_ENTER)
                    validator.validate();
                return false;
            }
        });
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    SoftKeyboardUtils.hide(v);
                return false;
            }
        });
        idEditText.addTextChangedListener(textWatcher);
        pwEditText.addTextChangedListener(textWatcher);
        validator.setValidationListener(this);
        SoftKeyboardNotifier.getDefault().register(this);
        EventBus.getDefault().register(this);
    }

    private ApiMember.JoinWithEmailReq getJoinWithEmailReq() {
        ApiMember.JoinWithEmailReq req = new ApiMember.JoinWithEmailReq();
        req.user_id = idEditText.getText().toString().trim();
        req.user_password = pwEditText.getText().toString().trim();
        req.user_email = email;
        return req;
    }

    private ApiMember.JoinWithIdpReq getJoinWithIdpReq() {
        ApiMember.JoinWithIdpReq req = new ApiMember.JoinWithIdpReq();
        req.user_id = idEditText.getText().toString().trim();
        req.user_password = pwEditText.getText().toString().trim();
        req.idp_type = ApiMember.IDProviderType.Facebook.value();
        req.idp_user_id = user.id;
        req.user_link = user.link;
        req.user_name = user.name;
        req.user_picture = user.picture.data.url;
        return req;
    }

    private void setEmail(String email) {
        this.email = email;

        if (email != null)
            extraTextView.setText(email);
    }

    private void setUser(Serializable user) {
        this.user = user != null ? (FBUser) user : null;

        if (this.user != null)
            extraTextView.setText(this.user.name);
    }
}
