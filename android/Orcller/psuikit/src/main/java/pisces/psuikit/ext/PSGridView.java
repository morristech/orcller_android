package pisces.psuikit.ext;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.widget.GridView;

import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/29/15.
 */
public class PSGridView extends GridView implements PSComponent {
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;

    public PSGridView(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public PSGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public PSGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperties(context, attrs, defStyleAttr, 0);
    }

    // ================================================================================================
    //  Overridden: GridView
    // ================================================================================================

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!initializedSubviews) {
            initializedSubviews = true;
            setUpSubviews(getContext());
        }

        invalidateProperties();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public int[] getCheckedPositions() {
        int[] positions = new int[getCheckedItemCount()];

        int idx = 0;
        SparseBooleanArray array = getCheckedItemPositions();
        for (int i=0; i<array.size(); i++) {
            int key = array.keyAt(i);
            if (array.get(key))
                positions[idx++] = key;
        }

        return positions;
    }

    public boolean isImmediatelyUpdating() {
        return immediatelyUpdating;
    }

    public void setImmediatelyUpdating(boolean immediatelyUpdating) {
        this.immediatelyUpdating = immediatelyUpdating;
    }

    public void invalidateProperties() {
        if (isAttachedToWindow() || immediatelyUpdating)
            commitProperties();
    }

    public void validateProperties() {
        commitProperties();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected void commitProperties() {
    }

    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    protected void setUpSubviews(Context context) {
    }
}