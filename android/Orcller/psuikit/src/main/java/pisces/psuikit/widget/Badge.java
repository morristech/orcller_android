package pisces.psuikit.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import pisces.android.R;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 12/18/15.
 */
public class Badge extends PSFrameLayout {
    private Bitmap maskBitmap;
    private Paint paint, maskPaint;
    private TextView textView;

    public Badge(Context context) {
        super(context);
    }

    public Badge(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public Badge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
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
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        textView = new TextView(context);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.leftMargin = GraphicUtils.convertDpToPixel(3);
        params.rightMargin = GraphicUtils.convertDpToPixel(3);

        textView.setTypeface(Typeface.DEFAULT_BOLD);
        setWillNotDraw(false);
        addView(textView, params);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Badge, 0, 0);
        try {
            setText(ta.getString(R.styleable.Badge_android_text));
            setTextColor(ta.getColorStateList(R.styleable.Badge_android_textColor));
            setTextSize((float) ta.getDimensionPixelSize(R.styleable.Badge_android_textSize, (int) getTextSize()));
        } finally {
            ta.recycle();
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public CharSequence getText() {
        return textView.getText();
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public ColorStateList getTextColor() {
        return textView.getTextColors();
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setTextColor(ColorStateList textColor) {
        if (textColor != null)
            textView.setTextColor(textColor);
    }

    public float getTextSize() {
        return textView.getTextSize();
    }

    public void setTextSize(float textSize) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
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
        canvas.drawRoundRect(new RectF(0, 0, width, height), height/2, height/2, paint);

        return mask;
    }
}
