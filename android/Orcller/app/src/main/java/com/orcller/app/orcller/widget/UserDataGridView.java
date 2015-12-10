package com.orcller.app.orcller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.AbstractDataGridItemView;
import com.orcller.app.orcller.model.album.ListEntity;
import com.orcller.app.orcller.proxy.UserDataProxy;
import com.orcller.app.orcllermodules.model.ApiResult;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSGridView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/11/15.
 */
public class UserDataGridView extends PSGridView implements AdapterView.OnItemClickListener {
    private int listCountAtOnce;
    private List<Model> items = new ArrayList<>();
    private Class<AbstractDataGridItemView> itemViewClass;
    private GridViewAdapter gridViewAdapter;
    private DataSource dataSource;
    private Delegate delegate;
    private ListEntity lastEntity;

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
    protected void commitProperties() {
    }

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
        setNumColumns(3);
        setHorizontalSpacing(GraphicUtils.convertDpToPixel(1));
        setVerticalSpacing(GraphicUtils.convertDpToPixel(1));
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
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public Class getItemViewClass() {
        return itemViewClass;
    }

    public void setItemViewClass(Class itemViewClass) {
        this.itemViewClass = itemViewClass;
    }

    public void setListCountAtOnce(int listCountAtOnce) {
        this.listCountAtOnce = listCountAtOnce;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (delegate != null)
            delegate.onSelect(position);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(final String after) {
        if (invalidDataLoading())
            return;

        UserDataProxy.getDefault().enqueueCall(
                dataSource.createDataLoadCall(listCountAtOnce, after),
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
                                    if (delegate != null)
                                        delegate.onLoad(lastEntity);

                                    endDataLoading();
                                    gridViewAdapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            if (BuildConfig.DEBUG)
                                Log.e("Api Error", response.body());
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (BuildConfig.DEBUG)
                            Log.e("onFailure", t);

                        endDataLoading();
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
            AbstractDataGridItemView itemView;

            if (convertView == null) {
                try {
                    Class<AbstractDataGridItemView> clazz = itemViewClass.getClass();
                    Constructor<itemViewClass> ctor = clazz.getConstructor(Context.class);


                    itemView = (AbstractDataGridItemView) ctor.newInstance(context);
                    itemView.setLayoutParams(new ViewGroup.LayoutParams(getColumnWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
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

    public static interface DataSource<T> {
        Call<T> createDataLoadCall(int limit, String after);
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public static interface Delegate {
        void onLoad(ListEntity listEntity);
        void onSelect(int position);
    }
}
