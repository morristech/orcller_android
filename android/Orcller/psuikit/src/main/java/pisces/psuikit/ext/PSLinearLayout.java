package pisces.psuikit.ext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by pisces on 11/22/15.
 */
abstract public class PSLinearLayout extends LinearLayout {
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;

    public PSLinearLayout(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public PSLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public PSLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperties(context, attrs, defStyleAttr, 0);
    }

    public PSLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initProperties(context, attrs, defStyleAttr, defStyleRes);
    }

    // ================================================================================================
    //  Overridden: RelativeLayout
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
    //  Protected
    // ================================================================================================

    abstract protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes);

    protected void commitProperties() {
    }

    protected void setUpSubviews(Context context) {
    }

    protected void invalidateProperties() {
        if (getParent() != null || immediatelyUpdating)
            commitProperties();
    }

    protected void validateProperties() {
        commitProperties();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void moveChildToBack(View child) {
        int index = indexOfChild(child);
        if (index > 0) {
            detachViewFromParent(index);
            attachViewToParent(child, 0, child.getLayoutParams());
        }
    }

    public boolean isImmediatelyUpdating() {
        return immediatelyUpdating;
    }

    public void setImmediatelyUpdating(boolean immediatelyUpdating) {
        this.immediatelyUpdating = immediatelyUpdating;
    }
}
