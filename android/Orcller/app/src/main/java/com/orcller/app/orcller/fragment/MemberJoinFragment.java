package com.orcller.app.orcller.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import pisces.psuikit.widget.ClearableEditText;
import pisces.psuikit.ext.PSFragment;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import pisces.psuikit.manager.ProgressBarManager;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;
import com.orcller.app.orcllermodules.utils.SoftKeyboardUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import de.greenrobot.event.EventBus;

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
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        editText = null;
        facebookButton = null;
        nextButton = null;
        orTextView = null;
        textWatcher = null;
        validator = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue()
                .getCallbackManager()
                .onActivityResult(requestCode, resultCode, data);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void loginWithFacebook() {
        if (invalidDataLoading())
            return;

        SoftKeyboardUtils.hide(getView());

        AuthenticationCenter.getDefault().loginWithFacebook(this, new FBSDKRequest.CompleteHandler() {
            @Override
            public void onComplete(JSONObject result, APIError error) {
                if (error == null)
                    ProgressBarManager.show(getActivity());
            }
        }, new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                ProgressBarManager.hide(getActivity());
                endDataLoading();

                if (error != null) {
                    if (error.getCode() == APIError.APIErrorCodeUserDoesNotExist) {
                        openMemberJoinInputActivity(result);
                    } else {
                        AlertDialogUtils.show(getContext(), error.getMessage(),
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
        ProgressBarManager.show(getActivity());

        AuthenticationCenter.getDefault().sendCertificationEmail(
                editText.getText().toString().trim(),
                new Api.CompleteHandler() {
                    @Override
                    public void onComplete(Object result, APIError error) {
                        endDataLoading();

                        if (error == null) {
                            ProgressBarManager.hide(getActivity(), ProgressBarManager.DISMISS_MODE_COMPLETE, new ProgressBarManager.DismissHandler() {
                                @Override
                                public void onDismiss() {
                                    AlertDialogUtils.show(
                                            getContext(),
                                            getResources().getString(R.string.m_send_email_result),
                                            getResources().getString(R.string.w_ok)
                                    );
                                    editText.setText(null);
                                }
                            });
                        } else {
                            ProgressBarManager.hide(getActivity(), ProgressBarManager.DISMISS_MODE_ERROR);
                        }
                    }
                });
    }

    // ================================================================================================
    //  Event Handler
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent.Show) {
            editText.setCursorVisible(true);
        } else if (event instanceof SoftKeyboardEvent.Hide) {
            editText.setCursorVisible(false);
        }
    }
}
