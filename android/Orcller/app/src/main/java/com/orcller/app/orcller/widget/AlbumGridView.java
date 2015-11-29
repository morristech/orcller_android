package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.itemview.AlbumGridItemView;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Page;

import java.util.List;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSGridView;

/**
 * Created by pisces on 11/29/15.
 */
public class AlbumGridView extends PSGridView implements AdapterView.OnItemClickListener {
    private int selectedIndex = -1;
    private int strokeWidth = 5;
    private Paint paint;
    private List<Integer> selectedIndexes;
    private Album model;
    private GridViewAdapter gridViewAdapter;
    private Delegate delegate;

    public AlbumGridView(Context context) {
        super(context);
    }

    public AlbumGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSGridView
    // ================================================================================================

    @Override
    protected void commitProperties() {
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        gridViewAdapter = new GridViewAdapter(context);
        paint = new Paint();

        paint.setColor(Color.RED);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);

        setBackgroundColor(Color.WHITE);
        setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        setNumColumns(4);
        setHorizontalSpacing(GraphicUtils.convertDpToPixel(1));
        setVerticalSpacing(GraphicUtils.convertDpToPixel(1));
        setOnItemClickListener(this);
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

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public Album getModel() {
        return model;
    }

    public void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        setAdapter(gridViewAdapter);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex == this.selectedIndex) {
            clearChoices();
            setSelection();
            return;
        }

        this.selectedIndex = selectedIndex;
        selectedIndexes = SharedObject.getListPositions(selectedIndex, model.pages.total_count);

        clearChoices();
        setSelection();
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setSelectedIndex(position);

        if (delegate != null)
            delegate.onSelect(position);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private int getDefaultPositin() {
        int row = Math.max(0, (model.default_page_index * 2) - 1);
        int next = getPosition(row + (row % 2 == 1 ? 1 : -1));
        return next > -1 ? next : getPosition(row);
    }

    private PageSelectionIndicatorView.ItemType getItemType(long position) {
        int count = getCount();
        if (position == 0 || (position >= count-1 && position%2 == 1))
            return PageSelectionIndicatorView.ItemType.Single;
        if (position%2 == 1)
            return PageSelectionIndicatorView.ItemType.Left;
        return PageSelectionIndicatorView.ItemType.Right;
    }

    private int getPosition(int row) {
        return row > -1 && row < model.pages.data.size() ? row : -1;
    }

    private void setSelection() {
        for (int index : selectedIndexes) {
            setItemChecked(index, true);
        }
    }

    // ================================================================================================
    //  Class: GridViewAdapter
    // ================================================================================================

    private class GridViewAdapter extends BaseAdapter {
        private Context context;

        public GridViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return model.pages.data.size();
        }

        @Override
        public Page getItem(int position) {
            return model.pages.data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AlbumGridItemView itemView;

            if (convertView == null) {
                itemView = new AlbumGridItemView(context);
                itemView.setLayoutParams(new ViewGroup.LayoutParams(getColumnWidth(), getColumnWidth()));
                convertView = itemView;
            } else {
                itemView = (AlbumGridItemView) convertView;
            }

            itemView.setAllowsShowDefaultIcon(getDefaultPositin() == position);
            itemView.setItemType(getItemType(position));
            itemView.setPage(getItem(position));

            return convertView;
        }
    }

    // ================================================================================================
    //  Class: GridViewAdapter
    // ================================================================================================

    public static interface Delegate {
        void onSelect(int position);
    }
}
