package pisces.psuikit.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import pisces.android.R;
import pisces.psfoundation.model.Resources;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/21/15.
 */
public class ExceptionView extends PSLinearLayout implements View.OnClickListener {
    private Delegate delegate;
    private TextView titleTextView;
    private TextView descTextView;
    private Button button;
    private ViewGroup viewGroup;

    public ExceptionView(Context context, ViewGroup viewGroup) {
        super(context);

        this.viewGroup = viewGroup;
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_exception, this);

        titleTextView = (TextView) findViewById(R.id.titleTextView);
        descTextView = (TextView) findViewById(R.id.descTextView);
        button = (Button) findViewById(R.id.button);

        descTextView.setGravity(Gravity.CENTER);
        button.setOnClickListener(this);
        setBackgroundColor(Color.WHITE);
        setClickable(true);
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

    public Button getButton() {
        return null;
    }

    public void setButtonText(CharSequence text) {
        button.setText(text);
        button.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setButtonText(int redId) {
        setButtonText(Resources.getString(redId));
    }

    public void setDescriptionText(CharSequence text) {
        descTextView.setText(text);
        descTextView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setDescriptionText(int redId) {
        setDescriptionText(Resources.getString(redId));
    }

    public void setTitleText(CharSequence text) {
        titleTextView.setText(text);
        titleTextView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setTitleText(int redId) {
        setTitleText(Resources.getString(redId));
    }

    public boolean validateException() {
        if (getDelegate() != null && !getDelegate().shouldShowExceptionView(this)) {
            viewGroup.removeView(this);
            return false;
        }

        viewGroup.addView(this, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return true;
    }

    public void removeFromParent() {
        if (viewGroup != null)
            viewGroup.removeView(this);
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    public void onClick(View v) {
        if (delegate != null)
            delegate.onClick(this);
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public interface Delegate {
        void onClick(ExceptionView view);
        boolean shouldShowExceptionView(ExceptionView view);
    }
}
