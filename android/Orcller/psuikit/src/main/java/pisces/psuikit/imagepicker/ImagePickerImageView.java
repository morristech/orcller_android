package pisces.psuikit.imagepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import pisces.android.R;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/25/15.
 */
public class ImagePickerImageView extends PSFrameLayout {
    private ProgressBar progressBar;
    private SubsamplingScaleImageView imageView;
    private Media media;
    private AsyncTask<Void, Void, Bitmap> task;

    public ImagePickerImageView(Context context) {
        super(context);
    }

    public ImagePickerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImagePickerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void commitProperties() {
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        imageView = new SubsamplingScaleImageView(context);
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);

        FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageViewParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

        LayoutParams progressBarParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        progressBarParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

        imageView.setDoubleTapZoomScale(3f);
        imageView.setMaxScale(6f);
        progressBar.setVisibility(GONE);
        setClipChildren(false);
        addView(imageView, imageViewParams);
        addView(progressBar, progressBarParams);
    }

    @Override
    protected void setUpSubviews(Context context) {
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Media getMedia() {
        return media;
    }

    public void setMedia(final Media media) {
        if (ObjectUtils.equals(media, this.media))
            return;

        if (task != null)
            task.cancel(true);

        imageView.recycle();
        imageView.resetScaleAndCenter();

        this.media = media;

        task = new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                return BitmapFactory.decodeFile(media.path, options);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);

                imageView.setImage(ImageSource.cachedBitmap(result));
            }
        }.execute();
    }

    public void reset() {
        imageView.resetScaleAndCenter();
    }
}
