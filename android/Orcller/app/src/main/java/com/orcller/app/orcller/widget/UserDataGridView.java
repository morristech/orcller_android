package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.AbstractDataGridItemView;
import com.orcller.app.orcller.itemview.LoadMoreFooterView;
import com.orcller.app.orcller.model.ListEntity;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.ApiResult;

import java.util.ArrayList;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSGridView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/11/15.
 */
public class UserDataGridView extends PSGridView implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private int listCountAtOnce;
    private ArrayList<Model> items = new ArrayList<>();
    private Class itemViewClass;
    private DataSource dataSource;
    private Delegate delegate;
    private ListEntity lastEntity;
    private GridViewAdapter gridViewAdapter;

    public UserDataGridView(Context context) {
        super(context);
    }

    public UserDataGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserDataGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSGridView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.UserDataGridView, defStyleAttr, defStyleRes);
        try {
            setListCountAtOnce(ta.getInt(R.styleable.UserDataGridView_listCountAtOnce, 20));
        } finally {
            ta.recycle();
        }

        gridViewAdapter = new GridViewAdapter(context);

        setAdapter(gridViewAdapter);
        setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        setDrawSelectorOnTop(true);
        setNumColumns(3);
        setHorizontalSpacing(GraphicUtils.convertDpToPixel(1));
        setVerticalSpacing(GraphicUtils.convertDpToPixel(1));
        setOnItemClickListener(this);
        setOnScrollListener(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public ArrayList<Model> getItems() {
        return items;
    }

    public void setItemViewClass(Class itemViewClass) {
        this.itemViewClass = itemViewClass;
    }

    public void setListCountAtOnce(int listCountAtOnce) {
        this.listCountAtOnce = listCountAtOnce;
    }

    public void reload() {
        load(null);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * AdapterView.OnItemClickListener
     */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (delegate != null)
            delegate.onSelect(this, position, items.get(position));
    }

    /**
     * AbsListView.OnScrollListener
     */
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (delegate != null)
            delegate.onScrollStateChanged(view, scrollState);
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        View itemView = view.getChildAt(0);
        Point point = itemView != null ? new Point(itemView.getLeft(), itemView.getTop()) : new Point();

        if (delegate != null)
            delegate.onScroll(view, point);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(final String after) {
        if (invalidDataLoading())
            return;

        load(dataSource.createDataLoadCall(listCountAtOnce, after), after);
    }

    private void load(Call call, final String after) {
        if (call == null)
            return;

        if (delegate != null)
            delegate.onLoad(this);

        final UserDataGridView self = this;

        UserDataProxy.getDefault().enqueueCall(
                call,
                new Callback<ApiResult>() {
                    @Override
                    public void onResponse(final Response<ApiResult> response, Retrofit retrofit) {
                        if (response.isSuccess() && response.body().isSuccess()) {
                            Application.run(new Runnable() {
                                @Override
                                public void run() {
                                    if (after == null)
                                        items.clear();

                                    lastEntity = (ListEntity) response.body().entity;

                                    items.addAll(lastEntity.data);
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    gridViewAdapter.notifyDataSetChanged();
                                    endDataLoading();

                                    if (delegate != null)
                                        delegate.onLoadComplete(self, lastEntity);
                                }
                            });
                        } else {
                            if (DEBUG)
                                e("Api Error", response.body());

                            endDataLoading();

                            if (delegate != null)
                                delegate.onFailure(self, new APIError(response.body()));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (BuildConfig.DEBUG)
                            Log.e("onFailure", t);

                        endDataLoading();

                        if (delegate != null)
                            delegate.onFailure(self, new Error(t.getMessage()));
                    }
                });
    }

    private void loadAfter() {
        if (lastEntity != null && lastEntity.after != null)
            load(lastEntity.after);
    }

    private void loadMore(int position) {
        if (lastEntity != null &&
                items.size() < lastEntity.total_count &&
                position >= items.size() - 9)
            loadAfter();
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
            return items != null ? items.size() : 0;
        }

        @Override
        public Model getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AbstractDataGridItemView itemView = null;

            if (convertView == null) {
                try {
                    itemView = (AbstractDataGridItemView) itemViewClass.getConstructor(Context.class).newInstance(context);
                    itemView.setLayoutParams(new AbsListView.LayoutParams(getColumnWidth(), itemView.getColumnHeight(getColumnWidth())));
                } catch (Exception e) {
                    if (BuildConfig.DEBUG)
                        Log.e(e.getMessage(), e);
                }

                convertView = itemView;
            } else {
                itemView = (AbstractDataGridItemView) convertView;
            }

            itemView.setModel(getItem(position));
            loadMore(position);

            return convertView;
        }
    }

    // ================================================================================================
    //  Interface: DataSource
    // ================================================================================================

    public interface DataSource<T> {
        Call<T> createDataLoadCall(int limit, String after);
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public interface Delegate {
        void onFailure(UserDataGridView gridView, Error error);
        void onLoad(UserDataGridView gridView);
        void onLoadComplete(UserDataGridView gridView, ListEntity listEntity);
        void onScroll(AbsListView view, Point scrollPoint);
        void onScrollStateChanged(AbsListView view, int scrollState);
        void onSelect(UserDataGridView gridView, int position, Model item);
    }
}
