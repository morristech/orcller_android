package pisces.psuikit.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Switch;

/**
 * Created by pisces on 12/23/15.
 */
public class ListSwitchItemView extends ListBaseItemView {
    private Switch aSwitch;

    public ListSwitchItemView(Context context) {
        super(context);
    }

    public ListSwitchItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListSwitchItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: ListBaseItemView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        aSwitch = new Switch(context);
        ViewGroup parentView = (ViewGroup) detailTextView.getParent();

        parentView.removeView(detailTextView);
        parentView.addView(aSwitch, detailTextView.getLayoutParams());
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Switch getSwitch() {
        return aSwitch;
    }
}

