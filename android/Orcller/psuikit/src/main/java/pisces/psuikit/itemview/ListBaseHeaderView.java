package pisces.psuikit.itemview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import pisces.android.R;
import pisces.psfoundation.model.Resources;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 11/27/15.
 */
public class ListBaseHeaderView extends PSLinearLayout {
    private TextView textView;

    public ListBaseHeaderView(Context context) {
        super(context);
    }

    public ListBaseHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListBaseHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, getLayoutResId(), this);

        textView = (TextView) findViewById(R.id.textView);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public CharSequence getText() {
        return textView.getText();
    }

    public void setText(CharSequence text) {
        textView.setText(text);
        textView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setText(int textResId) {
        setText(textResId > 0 ? Resources.getString(textResId) : null);
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected int getLayoutResId() {
        return R.layout.headerview_list_base;
    }
}
