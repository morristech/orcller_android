package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.orcller.app.orcller.R;

import java.util.List;

import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.itemview.ListBaseItemView;

/**
 * Created by pisces on 12/24/15.
 */
public class ChangePasswordListItemView extends ListBaseItemView implements Validator.ValidationListener {
    private boolean valid;

    @Length(min = 6, max = 16, messageResId = R.string.m_validate_password_length)
    private EditText editText;

    private Validator validator;

    public ChangePasswordListItemView(Context context) {
        super(context);
    }

    public ChangePasswordListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChangePasswordListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: ListBaseItemView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        editText = (EditText) findViewById(R.id.editText);
        validator = new Validator(this);

        editText.setCompoundDrawables(getDrawableLeft(), null, null, null);
        validator.setValidationListener(this);
    }

    @Override
    protected void inflateLayout(Context context) {
        inflate(context, R.layout.itemview_change_password, this);
    }

    @Override
    public String getTitleText() {
        return editText.getText().toString();
    }

    @Override
    public void setTitleText(CharSequence text) {
        editText.setText(text);
        editText.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    @Override
    public void setTitleText(int textResId) {
        setTitleText(textResId > 0 ? Resources.getString(textResId) : null);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public EditText getEditText() {
        return editText;
    }

    public boolean isValid() {
        return valid;
    }

    public void setHint(CharSequence hint) {
        editText.setHint(hint);
    }

    public void setHint(int hintResId) {
        setHint(hintResId > 0 ? Resources.getString(hintResId) : null);
    }

    public void validate() {
        validator.validate();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    public void onValidationSucceeded() {
        valid = true;
    }

    public void onValidationFailed(List<ValidationError> errors) {
        valid = false;

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

    // ================================================================================================
    //  Private
    // ================================================================================================

    private Drawable getDrawableLeft() {
        Drawable drawable = getResources().getDrawable(R.drawable.icon_locker_normal);
        drawable.setBounds(0, 0, GraphicUtils.convertDpToPixel(24), GraphicUtils.convertDpToPixel(24));
        return drawable;
    }
}
