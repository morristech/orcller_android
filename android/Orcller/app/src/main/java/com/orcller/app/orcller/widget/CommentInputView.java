package com.orcller.app.orcller.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.utils.SoftKeyboardUtils;

import de.greenrobot.event.EventBus;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/3/15.
 */
public class CommentInputView extends PSLinearLayout {
    private EditText editText;
    private Button postButton;
    private Delegate delegate;

    public CommentInputView(Context context) {
        super(context);
    }

    public CommentInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_comment_input, this);

        editText = (EditText) findViewById(R.id.editText);
        postButton = (Button) findViewById(R.id.postButton);

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                postButton.setEnabled(!TextUtils.isEmpty(editText.getText().toString().trim()));
            }
        });

        postButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.onClickPostButton();
            }
        });

        EventBus.getDefault().register(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public CharSequence getText() {
        return editText.getText();
    }

    public void clear() {
        editText.setText(null);
        editText.setCursorVisible(false);
        SoftKeyboardUtils.hide(this);
    }

    public void setFocus() {
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        SoftKeyboardUtils.show(editText);
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

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public static interface Delegate {
        void onClickPostButton();
    }
}
