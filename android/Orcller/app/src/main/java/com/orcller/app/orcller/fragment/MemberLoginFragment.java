package com.orcller.app.orcller.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.ext.ClearableEditText;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.managers.ProgressBarManager;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.api.ApiMember;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import com.orcller.app.orcllermodules.utils.SoftKeyboardUtils;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by pisces on 11/7/15.
 */
public class MemberLoginFragment extends Fragment {
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
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idEditText = (ClearableEditText) getView().findViewById(R.id.idEditText);
        pwEditText = (ClearableEditText) getView().findViewById(R.id.pwEditText);
        facebookButton = (Button) getView().findViewById(R.id.facebookButton);
        goButton = (Button) getView().findViewById(R.id.goButton);
        bottomContainer = (LinearLayout) getView().findViewById(R.id.bottomContainer);

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

        validator = new Validator(this);

        setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ProgressBarManager.hide(getActivity());
        idEditText.removeTextChangedListener(textWatcher);
        pwEditText.removeTextChangedListener(textWatcher);
        EventBus.getDefault().unregister(this);

        idEditText = null;
        pwEditText = null;
        facebookButton = null;
        goButton = null;
        textWatcher = null;
        bottomContainer = null;
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

//    private boolean invalidateEmail() {
//        String value = editText.getText().toString().trim();
//
//        if (TextUtils.isEmpty(value) ||
//                !Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
//
//            String text = TextUtils.isEmpty(value) ?
//                    "Email address is required!" :
//                    "Not the letter of the email address form!";
//
//            Toast toast = Toast.makeText(
//                    getActivity().getApplicationContext(),
//                    text, Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.TOP, 0, 0);
//            toast.show();
//
//            return true;
//        }
//
//        return false;
//    }

    private void login() {
        ApiMember.LoginReq req = new ApiMember.LoginReq();
        req.user_id = idEditText.getText().toString().trim();
        req.user_password = pwEditText.getText().toString().trim();

        AuthenticationCenter.getDefault().login(req, new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError err) {

            }
        });
    }

    private void loginWithFacebook() {
        SoftKeyboardUtils.hide(getView());
        ProgressBarManager.show(getActivity());

        AuthenticationCenter.getDefault().loginWithFacebook(this, new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                if (error != null) {
                    ProgressBarManager.hide(getActivity());

                    if (error.getCode() == APIError.APIErrorCodeUserDoesNotExist) {
                        openMemberJoinInputActivity(result);
                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setMessage("It failed to login.\nPlease try again.");
                        alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loginWithFacebook();
                            }
                        });
                        alertDialog.setNegativeButton("Dismiss", null);
                        alertDialog.show();
                    }
                } else {
                    ProgressBarManager.hide(getActivity(), ProgressBarManager.DISMISS_MODE_COMPLETE);
                }
            }
        });
    }

    private void openMemberJoinInputActivity(Object object) {

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
    //  Event Handler
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent.Show) {
            if (idEditText.hasFocus())
                idEditText.setCursorVisible(true);

            if (pwEditText.hasFocus())
                pwEditText.setCursorVisible(true);
        } else if (event instanceof SoftKeyboardEvent.Hide) {
            idEditText.setCursorVisible(false);
            pwEditText.setCursorVisible(false);
        }
    }
}
