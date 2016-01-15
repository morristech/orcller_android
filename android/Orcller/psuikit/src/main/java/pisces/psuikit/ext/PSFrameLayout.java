package pisces.psuikit.ext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import pisces.psfoundation.utils.DataLoadValidator;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/19/15.
 */
public class PSFrameLayout extends FrameLayout
        implements PSComponent, DataLoadValidator.Client, ViewTreeObserver.OnGlobalLayoutListener {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;

    public PSFrameLayout(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public PSFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public PSFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperties(context, attrs, defStyleAttr, 0);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    // ================================================================================================
    //  Overridden: FrameLayout
    // ================================================================================================

    @Override
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        if (!initializedSubviews) {
            initializedSubviews = true;

            setUpSubviews(getContext());
        }

        invalidateProperties();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void bringChildToFront(View child) {
        int index = indexOfChild(child);
        if (index >= 0 && index < getChildCount() - 1) {
            detachViewFromParent(index);
            addView(child, child.getLayoutParams());
        }
    }

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

    public boolean isFirstLoading() {
        return dataLoadValidator.isFirstLoading();
    }

    public void endDataLoading() {
        dataLoadValidator.endDataLoading();
    }

    public boolean invalidDataLoading() {
        return dataLoadValidator.invalidDataLoading();
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
