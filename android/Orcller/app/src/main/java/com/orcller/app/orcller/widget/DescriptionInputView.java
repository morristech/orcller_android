package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.utils.SoftKeyboardUtils;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/4/15.
 */
public class DescriptionInputView extends PSLinearLayout {
    private User model;
    private EditText editText;
    private UserPictureView userPictureView;

    public DescriptionInputView(Context context) {
        super(context);
    }

    public DescriptionInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DescriptionInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_description_input, this);

        editText = (EditText) findViewById(R.id.editText);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DescriptionInputView, defStyleAttr, defStyleRes);
        try {
            editText.setHint(ta.getString(R.styleable.DescriptionInputView_android_hint));
        } finally {
            ta.recycle();
        }

        EventBus.getDefault().register(this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        editText.setEnabled(enabled);
        userPictureView.setVisibility(enabled ? VISIBLE : GONE);
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

        userPictureView.setModel(model);
    }

    public CharSequence getText() {
        return editText.getText();
    }

    public void setText(CharSequence text) {
        editText.setText(text);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        editText.addTextChangedListener(watcher);
    }

    public void clear() {
        editText.setText(null);
        editText.setCursorVisible(false);
        SoftKeyboardUtils.hide(this);
    }

    public void removeTextChangedListener(TextWatcher watcher) {
        editText.removeTextChangedListener(watcher);
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
