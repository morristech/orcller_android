package pisces.psuikit.imagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.android.R;
import pisces.psfoundation.ext.Application;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSActionBarActivity;

/**
 * Created by pisces on 11/25/15.
 */
public class ImagePickerViewActivity extends PSActionBarActivity
        implements RecyclerViewPager.OnPageChangedListener {
    public static final String ITEMS_KEY = "items";
    public static final String SELECTED_INDEX_KEY = "selectedIndex";
    private int selectedIndex = -1;
    private List<Media> items;
    private TextView pagerTextView;
    private TextView toolbarTextView;
    private RecyclerViewPager recyclerView;
    private Adapter adapter;
    private ImagePickerImageView selectedView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imagepickerview);

        items = (List<Media>) getIntent().getSerializableExtra(ITEMS_KEY);

        recyclerView = (RecyclerViewPager) findViewById(R.id.recyclerView);
        toolbarTextView = (TextView) findViewById(R.id.toolbarTextView);
        pagerTextView = (TextView) findViewById(R.id.pagerTextView);
        adapter = new Adapter(items);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnPageChangedListener(this);
        recyclerView.addOnScrollListener(new OnScrollListener());
        setSelectedIndex(getIntent().getIntExtra(SELECTED_INDEX_KEY, 0));
        recyclerView.scrollToPosition(selectedIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recyclerView.removeOnPageChangedListener(this);

        adapter = null;
        items = null;
        toolbarTextView = null;
        recyclerView = null;
        pagerTextView = null;
        selectedView = null;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void OnPageChanged(int oldPosition, int newPosition) {
        boolean initialSelection = newPosition == selectedIndex;

        setSelectedIndex(newPosition);

        if (selectedView == null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedIndex);

            if (viewHolder != null)
                selectedView = (ImagePickerImageView) viewHolder.itemView;

            if (!initialSelection)
                EventBus.getDefault().post(new IndexChangeEvent(
                        IndexChangeEvent.INDEX_CHANGE, this, selectedView, selectedIndex));
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void startActivity(ArrayList<Media> items, int selectedIndex) {
        Intent intent = new Intent(Application.applicationContext(), ImagePickerViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ITEMS_KEY, items);
        intent.putExtra(SELECTED_INDEX_KEY, selectedIndex);
        Application.startActivity(intent, R.animator.fadein, R.animator.fadeout);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setSelectedIndex(int selectedIndex) {
        if (selectedIndex == this.selectedIndex)
            return;

        this.selectedIndex = selectedIndex;

        if (selectedView != null) {
            selectedView.reset();

            selectedView = null;
        }

        updateInfo();
    }

    private void updateInfo() {
        String text = String.valueOf(selectedIndex + 1) + " of " + String.valueOf(items.size());
        toolbarTextView.setText(items.get(selectedIndex).getDateString());
        pagerTextView.setText(text);
    }

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
            View view = new ImagePickerImageView(viewGroup.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(viewGroup.getWidth(), viewGroup.getHeight());
            view.setLayoutParams(params);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            ImagePickerImageView imagePickerImageView = (ImagePickerImageView) viewHolder.itemView;
            imagePickerImageView.setMedia(items.get(position));
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
}