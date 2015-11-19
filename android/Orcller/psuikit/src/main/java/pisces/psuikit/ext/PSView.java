package pisces.psuikit.ext;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/19/15.
 */
abstract public class PSView extends FrameLayout {
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;

    public PSView(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public PSView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public PSView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperties(context, attrs, defStyleAttr, 0);
    }

    public PSView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
    abstract protected void commitProperties();
    abstract protected void setUpSubviews(Context context);

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

    public boolean isImmediatelyUpdating() {
        return immediatelyUpdating;
    }

    public void setImmediatelyUpdating(boolean immediatelyUpdating) {
        this.immediatelyUpdating = immediatelyUpdating;
    }
}
