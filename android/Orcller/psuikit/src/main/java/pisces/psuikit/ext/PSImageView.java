package pisces.psuikit.ext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.ImageView;

import pisces.android.R;

/**
 * Created by pisces on 11/27/15.
 */
public class PSImageView extends ImageView implements PSComponent {
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;
    private float borderWidth;
    private float cornerRadius;
    private @ColorInt int borderColor;
    private Bitmap maskBitmap;
    private Paint paint, maskPaint, borderPaint;

    public PSImageView(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public PSImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public PSImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initProperties(context, attrs, defStyle, 0);
    }

    // ================================================================================================
    //  Overridden: ImageView
    // ================================================================================================

    @Override
    public void draw(Canvas canvas) {
        Bitmap offscreenBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas offscreenCanvas = new Canvas(offscreenBitmap);

        super.draw(offscreenCanvas);

        if (maskBitmap == null)
            maskBitmap = createMask(canvas.getWidth(), canvas.getHeight());

        offscreenCanvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint);
        canvas.drawBitmap(offscreenBitmap, 0f, 0f, paint);
        canvas.drawRoundRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()),
                cornerRadius, cornerRadius, borderPaint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!initializedSubviews) {
            initializedSubviews = true;
            setUpSubviews(getContext());
        }

        invalidateProperties();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isImmediatelyUpdating() {
        return immediatelyUpdating;
    }

    public void setImmediatelyUpdating(boolean immediatelyUpdating) {
        this.immediatelyUpdating = immediatelyUpdating;
    }

    public void setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;

        invalidate();
    }

    public void invalidateProperties() {
        if (isAttachedToWindow() || immediatelyUpdating)
            commitProperties();
    }

    public void validateProperties() {
        commitProperties();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected void commitProperties() {
    }

    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PSImageView, 0, 0);
        try {
            borderColor = ta.getColor(R.styleable.PSImageView_borderColor, 0xFFE6E6F4);
            borderWidth = ta.getDimension(R.styleable.PSImageView_strokeWidth, 0.0f);
            cornerRadius = ta.getDimension(R.styleable.PSImageView_cornerRadius, 0.0f);
        } finally {
            ta.recycle();
        }

        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);
        setWillNotDraw(false);
    }

    protected void setUpSubviews(Context context) {
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private Bitmap createMask(int width, int height) {
        Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(mask);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);

        canvas.drawRect(0, 0, width, height, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRoundRect(new RectF(0, 0, width, height), cornerRadius, cornerRadius, paint);

        return mask;
    }
}
