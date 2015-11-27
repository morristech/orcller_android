package pisces.psuikit.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import pisces.android.R;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 11/27/15.
 */
public class HeaderItemView extends PSLinearLayout {
    private TextView textView;

    public HeaderItemView(Context context) {
        super(context);
    }

    public HeaderItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_header, this);

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
    }
}
