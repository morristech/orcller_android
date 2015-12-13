package pisces.psuikit.imagepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;

import pisces.android.R;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/24/15.
 */
public class ImagePickerItemView extends PSFrameLayout implements Checkable {
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private boolean allowsShowIndicator = true;
    private boolean checked = false;
    private ImageView imageView;
    private FrameLayout selectionIndicator;
    private AsyncTask<Void, Void, Bitmap> task;
    private Media media;

    public ImagePickerItemView(Context context) {
        super(context);
    }

    public ImagePickerItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImagePickerItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_imagepicker, this);

        imageView = (ImageView) findViewById(R.id.imageView);
        selectionIndicator = (FrameLayout) findViewById(R.id.selectionIndicator);
    }

    // ================================================================================================
    //  Impl: Checkable
    // ================================================================================================

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked())
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked == this.checked)
            return;;

        this.checked = checked;

        refreshDrawableState();
        selectionIndicator.setVisibility(allowsShowIndicator && checked ? VISIBLE : INVISIBLE);
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isAllowsShowIndicator() {
        return allowsShowIndicator;
    }

    public void setAllowsShowIndicator(boolean allowsShowIndicator) {
        this.allowsShowIndicator = allowsShowIndicator;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setMedia(final Media media) {
        if (ObjectUtils.equals(media, this.media))
            return;

        if (task != null)
            task.cancel(true);

        imageView.setImageBitmap(null);

        this.media = media;

        task = new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return MediaStore.Images.Thumbnails.getThumbnail(
                        Application.applicationContext().getContentResolver(),
                        media.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);

                imageView.setImageBitmap(result);
            }
        }.execute();
    }
}
