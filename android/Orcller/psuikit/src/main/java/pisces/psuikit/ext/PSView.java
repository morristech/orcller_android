package pisces.psuikit.ext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import pisces.psfoundation.utils.DataLoadValidator;

/**
 * Created by pisces on 11/19/15.
 */
public class PSView extends View implements PSComponent {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
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

    // ================================================================================================
    //  Overridden: View
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

    public static boolean isShown(View view) {
        return view != null ? view.getVisibility() == View.VISIBLE : false;
    }

    public boolean isImmediatelyUpdating() {
        return immediatelyUpdating;
    }

    public void setImmediatelyUpdating(boolean immediatelyUpdating) {
        this.immediatelyUpdating = immediatelyUpdating;
    }

    public void invalidateProperties() {
        if (getParent() != null || immediatelyUpdating)
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

