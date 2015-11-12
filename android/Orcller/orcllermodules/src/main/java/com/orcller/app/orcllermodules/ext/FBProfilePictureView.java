package com.orcller.app.orcllermodules.ext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.facebook.login.widget.ProfilePictureView;
import com.orcller.app.orcllermodules.R;

/**
 * Created by pisces on 11/12/15.
 */
public class FBProfilePictureView extends ProfilePictureView {
    private float borderWidth;
    private float cornerRadius;
    private int borderColor;
    private Bitmap maskBitmap;
    private Bitmap borderBitmap;
    private Paint paint, maskPaint, borderPaint;

    public FBProfilePictureView(Context context) {
        super(context);

        init(context, null, 0);
    }

    public FBProfilePictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public FBProfilePictureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    // ================================================================================================
    //  Overridden: ProfilePictureView
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
        canvas.drawRoundRect(new RectF(borderWidth, borderWidth, canvas.getWidth(), canvas.getHeight()),
                cornerRadius, cornerRadius, borderPaint);
    }

    // ================================================================================================
    //  Overridden: Activity
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

    private void init(Context context, AttributeSet attrs, int defStyle) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FBProfilePictureView, 0, 0);
        try {
            borderColor = ta.getColor(R.styleable.FBProfilePictureView_borderColor, 0xFFE6E6F4);
            borderWidth = ta.getDimension(R.styleable.FBProfilePictureView_borderWidth, 0.0f);
            cornerRadius = ta.getDimension(R.styleable.FBProfilePictureView_cornerRadius, 0.0f);
        } finally {
            ta.recycle();
        }

        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);
        setWillNotDraw(false);
    }
}
