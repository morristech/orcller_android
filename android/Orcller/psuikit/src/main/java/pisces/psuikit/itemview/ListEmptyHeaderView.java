package pisces.psuikit.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import pisces.android.R;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/23/15.
 */
public class ListEmptyHeaderView extends PSLinearLayout {
    public ListEmptyHeaderView(Context context) {
        super(context);
    }

    public ListEmptyHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListEmptyHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.headerview_list_empty, this);
    }
}
