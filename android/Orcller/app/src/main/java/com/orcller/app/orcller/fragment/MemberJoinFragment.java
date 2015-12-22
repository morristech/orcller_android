package com.orcller.app.orcller.fragment;

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
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.MemberJoinInputActivity;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.facebook.FBUser;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;
import com.orcller.app.orcllermodules.utils.SoftKeyboardUtils;

import java.io.Serializable;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psuikit.ext.PSFragment;
import pisces.psuikit.manager.ProgressDialogManager;
import pisces.psuikit.widget.ClearableEditText;

/**
 * Created by pisces on 11/7/15.
 */
public class MemberJoinFragment extends PSFragment {
    @NotEmpty
    @Email
    private ClearableEditText editText;

    private Button facebookButton;
    private Button nextButton;
    private TextWatcher textWatcher;
    private TextView orTextView;
    private Validator validator;

    /**
     * @constructor
     */
    public MemberJoinFragment() {
    }

    // ================================================================================================
    //  Overridden: Fragment
    // ================================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_member_join, null);
    }

    @Override
    protected void setUpSubviews(View view) {
        editText = (ClearableEditText) getView().findViewById(R.id.editText);
        facebookButton = (Button) getView().findViewById(R.id.facebookButton);
        nextButton = (Button) getView().findViewById(R.id.nextButton);
        orTextView = (TextView) getView().findViewById(R.id.orTextView);
        textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmpty = TextUtils.isEmpty(editText.getText().toString().trim());
                nextButton.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                facebookButton.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                orTextView.setVisibility(facebookButton.getVisibility());
            }
        };

        validator = new Validator(this);

        setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        editText.removeTextChangedListener(textWatcher);
        facebookButton.setOnClickListener(null);
        nextButton.setOnClickListener(null);
        editText.setOnFocusChangeListener(null);
        editText.setOnKeyListener(null);
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
                        AlertDialogUtils.retry(error.getMessage(), new Runnable() {
                            @Override
                            public void run() {
                                loginWithFacebook();
                            }
                        });
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
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editText.setCursorVisible(hasFocus);
            }
        });
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == event.KEYCODE_ENTER)
                    validator.validate();
                return false;
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
                sendCertificationEmail();
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

        editText.addTextChangedListener(textWatcher);
        EventBus.getDefault().register(this);
    }

    private void sendCertificationEmail() {
        if (invalidDataLoading())
            return;

        SoftKeyboardUtils.hide(getView());
        ProgressDialogManager.show(R.string.w_processing);

        AuthenticationCenter.getDefault().sendCertificationEmail(
                editText.getText().toString().trim(),
                new Api.CompleteHandler() {
                    @Override
                    public void onComplete(Object result, APIError error) {
                        endDataLoading();

                        if (error == null) {
                            AlertDialogUtils.show(
                                    getResources().getString(R.string.m_complete_email_send),
                                    getResources().getString(R.string.w_ok)
                            );
                            editText.setText(null);
                        }
                    }
                });
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                editText.setCursorVisible(true);
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                editText.setCursorVisible(false);
            }
        }
    }
}
