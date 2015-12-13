package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.orcller.app.orcller.R;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.ext.PSView;

/**
 * Created by pisces on 12/12/15.
 */
public class UserPictureCropGuideView extends PSView {
    private Paint backgroundPaint;
    private Paint rectPaint;
    private Rect cropRect;

    public UserPictureCropGuideView(Context context) {
        super(context);
    }

    public UserPictureCropGuideView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserPictureCropGuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSView
    // ================================================================================================

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = w;
        int y = (getHeight() - h)/2;
        cropRect = new Rect(0, y, w, y + h);

        canvas.drawRect(0, 0, w, y, backgroundPaint);
        canvas.drawRect(0, y + h, w, getHeight(), backgroundPaint);
        canvas.drawRect(cropRect, rectPaint);
    }

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.argb(100, 0, 0, 0));
        backgroundPaint.setStyle(Paint.Style.FILL);

        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(Color.LTGRAY);
        rectPaint.setStrokeWidth(1);
        rectPaint.setStyle(Paint.Style.STROKE);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Rect getCropRect() {
        return cropRect;
    }
}
