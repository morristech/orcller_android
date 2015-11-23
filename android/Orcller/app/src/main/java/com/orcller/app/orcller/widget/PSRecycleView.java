package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/23/15.
 */
public class PSRecycleView extends AdapterView {

    private static final int ADD_VIEW_DIRECTION_TOP = 1;
    private static final int ADD_VIEW_DIRECTION_BOTTOM = 2;

    private int selectedIndex;
    private Adapter adapter = null;
    private Map<String, List<View>> recycleMap = new HashMap<String, List<View>>();

    public PSRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (adapter == null)
            return;

        float w = 0;
        int mw = getWidth() + getWidth()/2;
        int index = 0;

        while (w < mw) {
            String recycleListKey = String.valueOf(adapter.getItemViewType(index));
            List<View> recycleList = recycleMap.get(recycleListKey);
            View view = null;

            if (recycleList != null) {
                view = recycleList.get(0);
            }

            view = adapter.getView(index, view, this);

            if (view != null) {
                addView(view);
                w += view.getWidth();
            }

            index++;

            if (index >= adapter.getCount() - 1)
                break;
        }

        Log.i("w", w);
//
//
//
//        if (getChildCount() == 0) {        // No child registered.
//            int position = 0;
//            while ( position < adapter.getCount() - 1) {
//
//
//
//                final View child = adapter.getView(position, null, this);    // [1]
//                addViewAndMeasure(child, -1);    // [2], [3]
//                position++;
//            }
//        }
//
//        layoutItems();        // 자식 뷰의 화면 내 위치를 설정 (child.layout() 호출)        // [4]
//        invalidate();
    }

    @Override
    public void setSelection(int position) {
    }

    @Override
    public View getSelectedView() {
        return null;
    }

    @Override
    public Adapter getAdapter() {
        return adapter;
    }


    @Override
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;

        removeAllViewsInLayout();
        requestLayout();
    }

    private void addViewAndMeasure(View child, int direction) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        int index = 0;
        if(direction == ADD_VIEW_DIRECTION_TOP)
            index = 0;
        else
            index = -1;
        child.setDrawingCacheEnabled(true);        // drawing cache 사용. drawChild() 참고.
        addViewInLayout(child, -1, params, true);       // [2]

        int itemWidth = getWidth();
        child.measure(MeasureSpec.EXACTLY | itemWidth, MeasureSpec.UNSPECIFIED);    // [3]
    }



    /**
     * 자식 뷰가 표시될 화면 내 위치 설정
     * 자식 뷰 크기를 확정하기 위해 measuring 과정이 선행 되어야 함.
     */
    private void layoutItems() {        // [4]
        int top = 0;

        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);

            int width = child.getMeasuredWidth();    // Measuring 과정이 끝난 후 확정된 크기
            int height = child.getMeasuredHeight();
            int left = (getWidth() - width) / 2;

            child.layout(left, top, left + width, top + height);
            top += height;
        }
    }
}
