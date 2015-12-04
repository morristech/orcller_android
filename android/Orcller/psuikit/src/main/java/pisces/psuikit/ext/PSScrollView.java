package pisces.psuikit.ext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import pisces.psfoundation.utils.DataLoadValidator;

/**
 * Created by pisces on 11/22/15.
 */
public class PSScrollView extends ScrollView implements PSComponent {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;
    private boolean scrollable = true;

    public PSScrollView(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public PSScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public PSScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperties(context, attrs, defStyleAttr, 0);
    }

    // ================================================================================================
    //  Overridden: ScrollView
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!scrollable)
            return false;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (scrollable)
                    return super.onTouchEvent(ev);
                return scrollable;
            default:
                return super.onTouchEvent(ev);
        }
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

    public boolean isScrollable() {
        return scrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    public void invalidateProperties() {
        if (isAttachedToWindow() || immediatelyUpdating)
            commitProperties();
    }

    public void moveChildToBack(View child) {
        int index = indexOfChild(child);
        if (index > 0) {
            detachViewFromParent(index);
            attachViewToParent(child, 0, child.getLayoutParams());
        }
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
