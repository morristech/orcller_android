package pisces.psuikit.ext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TabHost;

import pisces.psfoundation.utils.DataLoadValidator;

/**
 * Created by pisces on 11/22/15.
 */
public class PSTabHost extends TabHost implements PSComponent {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;

    public PSTabHost(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public PSTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public PSTabHost(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperties(context, attrs, defStyleAttr, 0);
    }

    // ================================================================================================
    //  Overridden: LinearLayout
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

    public void invalidateProperties() {
        if (initializedSubviews || immediatelyUpdating)
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
