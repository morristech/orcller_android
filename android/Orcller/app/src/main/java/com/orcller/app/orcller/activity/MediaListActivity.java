package com.orcller.app.orcller.activity;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.widget.ImageMediaScrollView;
import com.orcller.app.orcller.widget.MediaScrollView;

import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSActionBarActivity;

/**
 * Created by pisces on 11/23/15.
 */
public class MediaListActivity extends PSActionBarActivity {
    private List<Media> items;
    private RecyclerViewPager recyclerView;
    private Adapter adapter;

    // ================================================================================================
    //  Overridden: PSActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_medialist);

        items = (List<Media>) getIntent().getSerializableExtra("items");

        recyclerView = (RecyclerViewPager) findViewById(R.id.recyclerView);
        adapter = new Adapter(items);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ItemDecoration(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i("scrollX", scrollX);
            }
        });
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        items = null;
        recyclerView = null;
        adapter = null;

        EventBus.getDefault().unregister(this);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof ImageMediaScrollView.ImageMediaScrollViewEvent) {
            ImageMediaScrollView.ImageMediaScrollViewEvent casted = (ImageMediaScrollView.ImageMediaScrollViewEvent) event;
            recyclerView.setLayoutFrozen(casted.getScaleFactor() > 1);
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================


    // ================================================================================================
    //  Class: Adapter
    // ================================================================================================

    private class Adapter extends RecyclerView.Adapter {
        List<Media> items;

        public Adapter(List<Media> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = new MediaScrollView(viewGroup.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(viewGroup.getWidth(), viewGroup.getHeight());
            view.setLayoutParams(params);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            MediaScrollView mediaScrollView = (MediaScrollView) viewHolder.itemView;
            Media model = items.get(position);
            mediaScrollView.setModel(model);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    // ================================================================================================
    //  Class: ViewHolder
    // ================================================================================================

    private final class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    // ================================================================================================
    //  Class: DividerItemDecoration
    // ================================================================================================

    public class ItemDecoration extends RecyclerView.ItemDecoration {

        public ItemDecoration(Context context) {
            super();
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            outRect.right = 20;
        }
    }
}
