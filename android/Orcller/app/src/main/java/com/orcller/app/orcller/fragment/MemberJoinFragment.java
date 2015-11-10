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
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.ext.ClearableEditText;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.managers.ProgressBarManager;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;
import com.orcller.app.orcllermodules.utils.Log;
import com.orcller.app.orcllermodules.utils.SoftKeyboardUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by pisces on 11/7/15.
 */
public class MemberJoinFragment extends Fragment {
    private ClearableEditText editText;
    private Button facebookButton;
    private Button nextButton;
    private TextWatcher textWatcher;
    private TextView orTextView;

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

        setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ProgressBarManager.hide(getActivity());
        editText.removeTextChangedListener(textWatcher);
        EventBus.getDefault().unregister(this);

        editText = null;
        facebookButton = null;
        nextButton = null;
        orTextView = null;
        textWatcher = null;
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

    private boolean invalidateEmail() {
        String value = editText.getText().toString().trim();

        if (TextUtils.isEmpty(value) ||
            !Patterns.EMAIL_ADDRESS.matcher(value).matches()) {

            String text = TextUtils.isEmpty(value) ?
                    "Email address is required!" :
                    "Not the letter of the email address form!";

            Toast toast = Toast.makeText(
                    getActivity().getApplicationContext(),
                    text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();

            return true;
        }

        return false;
    }

    private void joinByEmail() {
        if (invalidateEmail())
            return;

        SoftKeyboardUtils.hide(getView());
        ProgressBarManager.show(getActivity());

        AuthenticationCenter.getDefault().sendCertificationEmail(
                editText.getText().toString().trim(),
                new Api.CompleteHandler() {
                    @Override
                    public void onComplete(Object result, APIError error) {
                        if (error == null) {
                            ProgressBarManager.hide(getActivity(), ProgressBarManager.DISMISS_MODE_COMPLETE, new ProgressBarManager.DismissHandler() {
                                @Override
                                public void onDismiss() {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                    alertDialog.setMessage("It has completed the certification -mail transmission.\n\nOpen an e-mail within 24 hours , please proceed to certification.");
                                    alertDialog.setPositiveButton("Ok", null);
                                    alertDialog.show();
                                }
                            });
                        } else {
                            ProgressBarManager.hide(getActivity(), ProgressBarManager.DISMISS_MODE_ERROR);
                        }
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
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinByEmail();
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
                    joinByEmail();
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
        editText.addTextChangedListener(textWatcher);
        EventBus.getDefault().register(this);
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
