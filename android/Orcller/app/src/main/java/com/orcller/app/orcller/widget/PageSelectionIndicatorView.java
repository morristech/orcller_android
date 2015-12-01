package com.orcller.app.orcller.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.orcller.app.orcller.R;

import pisces.psuikit.ext.PSView;

/**
 * Created by pisces on 11/29/15.
 */
public class PageSelectionIndicatorView extends PSView {
    public enum ItemType {
        Left(1),
        Right(2),
        Single(3);

        private int value;

        private ItemType(int value) {
            this.value = value;
        }
    }

    private int strokeWidth = 8;
    private Paint paint;
    private Path path;
    private ItemType itemType;

    public PageSelectionIndicatorView(Context context) {
        super(context);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();

        paint.setColor(getResources().getColor(R.color.theme_lightgray_toolbar_control));
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
    }

    // ================================================================================================
    //  Overridden: PSView
    // ================================================================================================

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        int sw = strokeWidth/2;

        canvas.drawColor(getResources().getColor(pisces.android.R.color.imagepicker_toolbar_color));
        path.reset();

        if (itemType.equals(ItemType.Single)) {
            path.moveTo(sw, sw);
            path.lineTo(w - sw, sw);
            path.lineTo(w - sw, h - sw);
            path.lineTo(sw, h - sw);
            path.lineTo(sw, sw);
        } else if (itemType.equals(ItemType.Left)) {
            path.moveTo(w + strokeWidth, sw);
            path.lineTo(sw, sw);
            path.lineTo(sw, h - sw);
            path.lineTo(w + strokeWidth, h - sw);
        } else if (itemType.equals(ItemType.Right)) {
            path.moveTo(-strokeWidth, sw);
            path.lineTo(w - sw, sw);
            path.lineTo(w - sw, h - sw);
            path.lineTo(-strokeWidth, h - sw);
        }

        path.close();
        canvas.drawPath(path, paint);
    }

    public PageSelectionIndicatorView.ItemType getItemType() {
        return itemType;
    }

    public void setItemType(PageSelectionIndicatorView.ItemType itemType) {
        this.itemType = itemType;

        invalidate();
    }
}
