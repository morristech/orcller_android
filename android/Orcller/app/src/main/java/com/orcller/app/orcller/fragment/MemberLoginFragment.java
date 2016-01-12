package com.orcller.app.orcller.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.orcller.app.orcller.AnalyticsTrackers;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.MemberJoinInputActivity;
import com.orcller.app.orcllermodules.error.APIError;
import pisces.psuikit.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import com.orcller.app.orcllermodules.model.facebook.FBUser;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import pisces.psuikit.utils.AlertDialogUtils;
import pisces.psuikit.keyboard.SoftKeyboardUtils;

import java.io.Serializable;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psuikit.ext.PSFragment;
import pisces.psuikit.manager.ProgressDialogManager;
import pisces.psuikit.widget.ClearableEditText;

/**
 * Created by pisces on 11/7/15.
 */
public class MemberLoginFragment extends PSFragment {
    @NotEmpty
    private ClearableEditText idEditText;

    @NotEmpty
    private ClearableEditText pwEditText;

    private Button facebookButton;
    private Button goButton;
    private TextWatcher textWatcher;
    private LinearLayout bottomContainer;
    private Validator validator;

    /**
     * @constructor
     */
    public MemberLoginFragment() {
    }

    // ================================================================================================
    //  Overridden: Fragment
    // ================================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_member_login, null);
    }

    @Override
    protected void setUpSubviews(View view) {
        idEditText = (ClearableEditText) getView().findViewById(R.id.idEditText);
        pwEditText = (ClearableEditText) getView().findViewById(R.id.pwEditText);
        facebookButton = (Button) getView().findViewById(R.id.facebookButton);
        goButton = (Button) getView().findViewById(R.id.goButton);
        bottomContainer = (LinearLayout) getView().findViewById(R.id.bottomContainer);
        validator = new Validator(this);
        textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmptyId = TextUtils.isEmpty(idEditText.getText().toString().trim());
                boolean isEmptyPw = TextUtils.isEmpty(pwEditText.getText().toString().trim());
                bottomContainer.setVisibility(isEmptyId ? View.VISIBLE : View.GONE);
                goButton.setVisibility(isEmptyId || isEmptyPw ? View.GONE : View.VISIBLE);
            }
        };

        setListeners();
        AnalyticsTrackers.getInstance().trackScreen(AnalyticsTrackers.Target.APP, getClass().getName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        idEditText.removeTextChangedListener(textWatcher);
        pwEditText.removeTextChangedListener(textWatcher);
        facebookButton.setOnClickListener(null);
        idEditText.setOnFocusChangeListener(null);
        pwEditText.setOnFocusChangeListener(null);
        pwEditText.setOnKeyListener(null);
        goButton.setOnClickListener(null);
        getView().setOnTouchListener(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        ProgressDialogManager.hide();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void login() {
        if (invalidDataLoading())
            return;

        SoftKeyboardUtils.hide(getView());
        ProgressDialogManager.show(R.string.w_login);

        ApiMember.LoginReq req = new ApiMember.LoginReq();
        req.user_id = idEditText.getText().toString().trim();
        req.user_password = pwEditText.getText().toString().trim();

        AuthenticationCenter.getDefault().login(req, new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, final APIError error) {
                endDataLoading();

                if (error != null && error.getMessage() != null) {
                    AlertDialogUtils.retry(error.getMessage(), new Runnable() {
                        @Override
                        public void run() {
                            login();
                        }
                    });
                }
            }
        });
    }

    private void loginWithFacebook() {
        if (invalidDataLoading())
            return;

        SoftKeyboardUtils.hide(getView());

        AuthenticationCenter.getDefault().loginWithFacebook(this, new FBSDKRequest.CompleteHandler<FBUser>() {
            @Override
            public void onComplete(FBUser result, APIError error) {
                if (error == null)
                    ProgressDialogManager.show(R.string.w_login);
            }
        }, new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                endDataLoading();

                if (error != null) {
                    if (error.getCode() == APIError.APIErrorCodeUserDoesNotExist) {
                        openMemberJoinInputActivity(result);
                    } else {
                        AlertDialogUtils.show(error.getMessage(),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == AlertDialog.BUTTON_POSITIVE)
                                            loginWithFacebook();
                                    }
                                },
                                getResources().getString(R.string.w_dismiss),
                                getResources().getString(R.string.w_retry)
                        );
                    }
                }
            }
        });
    }

    private void openMemberJoinInputActivity(Object object) {
        Intent intent = new Intent(getActivity(), MemberJoinInputActivity.class);
        intent.putExtra("user", (Serializable) object);
        startActivity(intent);
    }

    private void setListeners() {
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithFacebook();
            }
        });
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
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });
        getView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    SoftKeyboardUtils.hide(v);
                return false;
            }
        });
        validator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                login();
            }

            @Override
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
        });

        idEditText.addTextChangedListener(textWatcher);
        pwEditText.addTextChangedListener(textWatcher);
        EventBus.getDefault().register(this);
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
}
