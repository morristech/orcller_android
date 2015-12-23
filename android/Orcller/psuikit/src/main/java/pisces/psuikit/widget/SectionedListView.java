package pisces.psuikit.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSListView;
import pisces.psuikit.itemview.ListBaseItemView;

/**
 * Created by pisces on 12/23/15.
 */
public class SectionedListView extends PSListView implements AdapterView.OnItemClickListener {
    private ListAdapter listAdapter;
    private DataSource dataSource;
    private Delegate delegate;

    public SectionedListView(Context context) {
        super(context);
    }

    public SectionedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SectionedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSListView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        listAdapter = new ListAdapter(this);

        setDivider(null);
        setBackgroundColor(Color.WHITE);
        setOnItemClickListener(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;

        setAdapter(listAdapter);
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public void reload() {
        listAdapter.notifyDataSetChanged();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (delegate != null)
            delegate.onItemClick(this, view, listAdapter.getIndexPath(position));
    }

    // ================================================================================================
    //  Class: ListAdapter
    // ================================================================================================

    private class ListAdapter extends BaseAdapter {
        private static final int NONE = -2;
        private static final int SECTION = -1;
        private int itemCount;
        private boolean shouldReload;
        private List<Integer> sections;
        private Map<String, IndexPath> indexPathMap;
        private ListView listView;

        public ListAdapter(ListView listView) {
            this.listView = listView;
        }

        @Override
        public void notifyDataSetChanged() {
            shouldReload = true;

            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (dataSource != null) {
                if (shouldReload) {
                    itemCount = 0;
                    sections = new ArrayList<>();
                    indexPathMap = new HashMap<>();

                    for (int i=0; i<dataSource.getSectionCount(listView); i++) {
                        sections.add(Integer.valueOf(itemCount));
                        itemCount += dataSource.getRowCount(listView, i) + 1;
                    }
                    shouldReload = false;
                }
                return itemCount;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 0 || position > itemCount - 1)
                return NONE;
            if (sections.contains(Integer.valueOf(position)))
                return SECTION;
            return dataSource.getItemViewType(listView, getIndexPath(position));
        }

        @Override
        public int getViewTypeCount() {
            return dataSource.getItemViewTypeCount(listView) + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);

            switch (type) {
                case SECTION:
                    convertView = dataSource.getHeaderView(listView, convertView, sections.indexOf(Integer.valueOf(position)));
                    break;

                default:
                    IndexPath indexPath = getIndexPath(position);
                    convertView = dataSource.getItemView(listView, convertView, indexPath);

                    if (convertView instanceof ListBaseItemView) {
                        ListBaseItemView view = (ListBaseItemView) convertView;
                        int rowCount = dataSource.getRowCount(listView, indexPath.section());

                        if (indexPath.row() == 0) {
                            if (rowCount == 1)
                                view.setLineDirection(ListBaseItemView.LINE_TOP | ListBaseItemView.LINE_BOTTOM);
                            else if (rowCount == 2)
                                view.setLineDirection(ListBaseItemView.LINE_TOP | ListBaseItemView.LINE_MIDDLE);
                            else
                                view.setLineDirection(ListBaseItemView.LINE_TOP);
                        } else if (indexPath.row() == rowCount - 1) {
                            view.setLineDirection(ListBaseItemView.LINE_BOTTOM);
                        } else {
                            view.setLineDirection(ListBaseItemView.LINE_MIDDLE);
                        }
                    }

                    break;
            }

            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != SECTION;
        }

        public IndexPath getIndexPath(int position) {
            String key = String.valueOf(position);

            if (indexPathMap.containsKey(key))
                return indexPathMap.get(key);

            int section = 0;
            int row = 0;
            for (int i=0; i<sections.size(); i++) {
                int start = sections.get(i).intValue();
                int end = start + dataSource.getRowCount(listView, i);
                if (position <= end) {
                    section = i;
                    row = position - start - 1;
                    break;
                }
            }

            IndexPath indexPath = new IndexPath(section, row);
            indexPathMap.put(key, indexPath);

            return indexPath;
        }
    }

    // ================================================================================================
    //  Interface: DataSource
    // ================================================================================================

    public interface DataSource {
        View getHeaderView(ListView listView, View convertView, int section);
        View getItemView(ListView listView, View convertView, IndexPath indexPath);
        int getRowCount(ListView listView, int section);
        int getSectionCount(ListView listView);
        int getItemViewType(ListView listView, IndexPath indexPath);
        int getItemViewTypeCount(ListView listView);
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public interface Delegate {
        void onItemClick(ListView listView, View view, IndexPath indexPath);
    }

    // ================================================================================================
    //  Class: IndexPath
    // ================================================================================================

    public static class IndexPath implements Serializable {
        private int row;
        private int section;

        public IndexPath(int section, int row) {
            this.section = section;
            this.row = row;
        }

        public int row() {
            return row;
        }

        public int section() {
            return section;
        }
    }
}
