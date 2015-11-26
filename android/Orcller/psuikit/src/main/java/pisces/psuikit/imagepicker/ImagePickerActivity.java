package pisces.psuikit.imagepicker;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import pisces.android.R;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/24/15.
 */
public class ImagePickerActivity extends PSActionBarActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {
    private ArrayList<Media> items = new ArrayList<Media>();
    private Button selectButton;
    private GridView gridView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imagepicker);

        gridView = (GridView) findViewById(R.id.gridView);
        selectButton = (Button) findViewById(R.id.selectButton);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getResources().getString(R.string.imagepicker_title));
        init();
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        selectButton.setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        gridView.setOnItemClickListener(null);
        gridView.setOnItemLongClickListener(null);
        selectButton.setOnClickListener(null);

        items = null;
        gridView = null;
        selectButton = null;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    @Override
    public void onClick(View v) {
        Log.i("onClick");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int count = gridView.getCheckedItemCount();
        String prefix = getResources().getString(R.string.imagepicker_menu_select);
        String text = count > 0 ? prefix + " " + String.valueOf(count) : prefix;

        selectButton.setEnabled(gridView.getCheckedItemCount() > 0);
        selectButton.setText(text);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ImagePickerViewActivity.startActivity(items, position);
        return true;
    }

    public void onEventMainThread(Object event) {
        if (event instanceof ImagePickerViewActivity.OnChangeSelectedIndex) {
            ImagePickerViewActivity.OnChangeSelectedIndex casted = (ImagePickerViewActivity.OnChangeSelectedIndex) event;
            gridView.setSelection(casted.getSelectedIndex());
        }
    }
    // ================================================================================================
    //  Private
    // ================================================================================================

    private void init() {
        gridView.setVisibility(View.INVISIBLE);
        ProgressBarManager.show(this);

        String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID,
                MediaStore.Images.ImageColumns.DESCRIPTION,
                MediaStore.Images.ImageColumns.DATE_TAKEN
        };
        CursorLoader cursorLoader = new CursorLoader(
                this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Images.Media._ID + " DESC");
        final Cursor cursor = cursorLoader.loadInBackground();
        final int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        final int count = cursor.getCount();
        final ImagePickerActivity self = this;

        Application.run(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    cursor.moveToPosition(i);

                    Media item = new Media();
                    item.id = cursor.getInt(columnIndex);
                    item.dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    item.path = cursor.getString(item.dataColumnIndex);
                    item.date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));

                    items.add(item);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                ProgressBarManager.hide(self);
                gridView.setVisibility(View.VISIBLE);
                gridView.setAdapter(new ImageAdapter(self));
            }
        });
    }

    // ================================================================================================
    //  Class: ImageAdapter
    // ================================================================================================

    private class ImageAdapter extends BaseAdapter {
        private Context context;

        public ImageAdapter(Context context) {
            this.context = context;
        }

        public int getCount() {
            return items.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImagePickerItemView itemView;

            if (convertView == null) {
                itemView = new ImagePickerItemView(context);
                itemView.setLayoutParams(new ViewGroup.LayoutParams(gridView.getColumnWidth(), gridView.getColumnWidth()));

                convertView = itemView;
            } else {
                itemView = (ImagePickerItemView) convertView;
            }

            itemView.setMedia(items.get(position));

            return convertView;
        }
    }
}
