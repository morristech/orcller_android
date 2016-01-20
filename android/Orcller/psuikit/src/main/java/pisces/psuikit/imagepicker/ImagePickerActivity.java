package pisces.psuikit.imagepicker;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.android.R;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.BitmapUtils;
import pisces.psuikit.event.ImagePickerEvent;
import pisces.psuikit.event.IndexChangeEvent;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/24/15.
 */
public class ImagePickerActivity extends PSActionBarActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final String CHOICE_MODE_KEY = "choiceMode";
    private ArrayList<Media> items = new ArrayList<>();
    private GridView gridView;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imagepicker);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getResources().getString(R.string.imagepicker_title));

        gridView = (GridView) findViewById(R.id.gridView);

        init();
        gridView.setChoiceMode(getIntent().getIntExtra(CHOICE_MODE_KEY, AbsListView.CHOICE_MODE_MULTIPLE));
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (gridView.getChoiceMode() > AbsListView.CHOICE_MODE_SINGLE) {
            Application.getTopActivity().getMenuInflater().inflate(pisces.android.R.menu.menu_image_picker, menu);

            MenuItem item = menu.findItem(pisces.android.R.id.select);
            item.setEnabled(gridView.getCheckedItemCount() > 0);
            int count = gridView.getCheckedItemCount();
            String prefix = getResources().getString(pisces.android.R.string.imagepicker_menu_select);
            String text = count > 0 ? prefix + " " + String.valueOf(count) : prefix;

            item.setTitle(text);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.select) {
            select();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        gridView.setOnItemClickListener(null);
        gridView.setOnItemLongClickListener(null);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(int choiceMode) {
        Intent intent = new Intent(Application.applicationContext(), ImagePickerActivity.class);
        intent.putExtra(CHOICE_MODE_KEY, choiceMode);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (gridView.getChoiceMode() > AbsListView.CHOICE_MODE_SINGLE)
            invalidateOptionsMenu();
        else
            select();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ImagePickerViewActivity.startActivity(items, position);
        return true;
    }

    public void onEventMainThread(Object event) {
        if (event instanceof IndexChangeEvent) {
            IndexChangeEvent casted = (IndexChangeEvent) event;

            if (casted.getTarget() instanceof ImagePickerViewActivity)
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
                MediaStore.Images.ImageColumns.WIDTH,
                MediaStore.Images.ImageColumns.HEIGHT,
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
                    item.width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH));
                    item.height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT));
                    item.date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));

                    try {
                        ExifInterface exifInterface = new ExifInterface(item.path);
                        item.orientation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
                    } catch (IOException e) {
                    }

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

    public void select() {
        final List<Media> list = new ArrayList<>();
        final Object self = this;

        Application.run(new Runnable() {
            @Override
            public void run() {
                SparseBooleanArray array = gridView.getCheckedItemPositions();
                for (int i=0; i<array.size(); i++) {
                    int key = array.keyAt(i);
                    if (array.get(key))
                        list.add(items.get(key));
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(
                        new ImagePickerEvent(
                                ImagePickerEvent.COMPLETE_SELECTION,
                                self,
                                list));
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
                itemView.setAllowsShowIndicator(gridView.getChoiceMode() > AbsListView.CHOICE_MODE_SINGLE);
                itemView.setLayoutParams(new AbsListView.LayoutParams(gridView.getColumnWidth(), gridView.getColumnWidth()));
                convertView = itemView;
            } else {
                itemView = (ImagePickerItemView) convertView;
            }

            itemView.setMedia(items.get(position));

            return convertView;
        }
    }
}
