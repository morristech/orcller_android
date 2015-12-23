package pisces.psuikit.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.widget.Badge;

/**
 * Created by pisces on 12/23/15.
 */
public class ListBadgeItemView extends ListBaseItemView {
    private Badge badge;

    public ListBadgeItemView(Context context) {
        super(context);
    }

    public ListBadgeItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListBadgeItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: ListBaseItemView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        badge = new Badge(context);
        ViewGroup parentView = (ViewGroup) detailTextView.getParent();
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, GraphicUtils.convertDpToPixel(20));
        params.gravity = Gravity.CENTER_VERTICAL;

        badge.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
        badge.setMinimumWidth(GraphicUtils.convertDpToPixel(20));
        badge.setTextColor(context.getResources().getColor(android.R.color.white));
        badge.setTextSize(13);
        parentView.addView(badge, 1, params);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Badge getBadge() {
        return badge;
    }
}

