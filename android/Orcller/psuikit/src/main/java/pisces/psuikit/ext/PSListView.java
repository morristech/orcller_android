package pisces.psuikit.ext;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.widget.ListView;

import pisces.psfoundation.utils.DataLoadValidator;

/**
 * Created by pisces on 12/5/15.
 */
public class PSListView extends ListView implements PSComponent, DataLoadValidator.Client, Scrollable {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;
    private boolean scrollable = true;

    public PSListView(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public PSListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public PSListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperties(context, attrs, defStyleAttr, 0);
    }

    // ================================================================================================
    //  Overridden: ListView
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec;

        if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
            heightSpec = MeasureSpec.makeMeasureSpec(
                    Integer.MAX_VALUE, MeasureSpec.AT_MOST);
        } else {
            heightSpec = heightMeasureSpec;
        }

        super.onMeasure(widthMeasureSpec, heightSpec);
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

    public boolean isScrollable() {
        return scrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    public void invalidateProperties() {
        if (initializedSubviews || immediatelyUpdating)
            commitProperties();
    }

    public void validateProperties() {
        commitProperties();
    }

    public void endDataLoading() {
        dataLoadValidator.endDataLoading();
    }

    public boolean isFirstLoading() {
        return dataLoadValidator.isFirstLoading();
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