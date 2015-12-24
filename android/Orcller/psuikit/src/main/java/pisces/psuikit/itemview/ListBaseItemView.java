package pisces.psuikit.itemview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import pisces.android.R;
import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 12/23/15.
 */
public class ListBaseItemView extends PSFrameLayout {
    public static final int LINE_NONE = 0;
    public static final int LINE_TOP = 1 << 0;
    public static final int LINE_MIDDLE = 1 << 1;
    public static final int LINE_BOTTOM = 1 << 2;
    protected TextView titleTextView;
    protected TextView subtitleTextView;
    protected TextView detailTextView;
    protected Background background;

    public ListBaseItemView(Context context) {
        super(context);
    }

    public ListBaseItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListBaseItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflateLayout(context);

        background = new Background(context);

        addView(background, 0);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public CharSequence getDetailText() {
        return detailTextView.getText();
    }

    public void setDetailText(CharSequence text) {
        detailTextView.setText(text);
        detailTextView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setDetailText(int textResId) {
        setDetailText(textResId > 0 ? Resources.getString(textResId) : null);
    }

    public void setDetailTextColor(int color) {
        detailTextView.setTextColor(color);
    }

    public int getLineDirection() {
        return background.getLineDirection();
    }

    public void setLineDirection(int lineDirection) {
        background.setLineDirection(lineDirection);
    }

    public int getLinePaddingLeft() {
        return background.linePaddingLeft;
    }

    public void setLinePaddingLeft(int left) {
        background.setLinePaddingLeft(left);
    }

    public int getLinePaddingRight() {
        return background.linePaddingRight;
    }

    public void setLinePaddingRight(int right) {
        background.setLinePaddingRight(right);
    }

    public CharSequence getTitleText() {
        return titleTextView.getText();
    }

    public void setTitleText(CharSequence text) {
        titleTextView.setText(text);
        titleTextView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setTitleText(int textResId) {
        setTitleText(textResId > 0 ? Resources.getString(textResId) : null);
    }

    public void setTitleTextColor(int color) {
        titleTextView.setTextColor(color);
    }

    public CharSequence getSubtitleText() {
        return subtitleTextView.getText();
    }

    public void setSubtitleText(CharSequence text) {
        subtitleTextView.setText(text);
        subtitleTextView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setSubtitleText(int textResId) {
        setSubtitleText(textResId > 0 ? Resources.getString(textResId) : null);
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected void inflateLayout(Context context) {
        inflate(context, R.layout.itemview_list_base, this);

        detailTextView = (TextView) findViewById(R.id.detailTextView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        subtitleTextView = (TextView) findViewById(R.id.subtitleTextView);
    }

    // ================================================================================================
    //  Class: Background
    // ================================================================================================

    public static class Background extends View {
        private int lineDirection = LINE_NONE;
        private int linePaddingLeft = GraphicUtils.convertDpToPixel(15);
        private int linePaddingRight;
        private Paint paint;
        private Path path;

        public Background(Context context) {
            super(context);

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            path = new Path();

            paint.setColor(getResources().getColor(R.color.itemview_list_base_background_line));
            paint.setStrokeWidth(GraphicUtils.convertDpToPixel(1));
            paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();
            int direction = getLineDirection();

            path.reset();

            if ((direction & LINE_TOP) == LINE_TOP) {
                path.moveTo(0, 0);
                path.lineTo(w, 0);
            }

            if ((direction & LINE_MIDDLE) == LINE_MIDDLE) {
                if ((direction & LINE_TOP) != LINE_TOP) {
                    path.moveTo(linePaddingLeft, 0);
                    path.lineTo(w - linePaddingRight, 0);
                }

                path.moveTo(linePaddingLeft, h);
                path.lineTo(w - linePaddingRight, h);
            }

            if ((direction & LINE_BOTTOM) == LINE_BOTTOM) {
                path.moveTo(0, h);
                path.lineTo(w, h);
            }

            path.close();
            canvas.drawPath(path, paint);
        }

        public int getLineDirection() {
            return lineDirection;
        }

        public void setLineDirection(int lineDirection) {
            if (lineDirection == this.lineDirection)
                return;

            this.lineDirection = lineDirection;
            invalidate();
        }

        public int getLinePaddingLeft() {
            return linePaddingLeft;
        }

        public void setLinePaddingLeft(int left) {
            if (left == this.linePaddingLeft)
                return;

            linePaddingLeft = left;
            invalidate();
        }

        public int getLinePaddingRight() {
            return linePaddingRight;
        }

        public void setLinePaddingRight(int right) {
            if (right == this.linePaddingRight)
                return;

            linePaddingRight = right;
            invalidate();
        }

        public int getStrokeColor() {
            return paint.getColor();
        }

        public void setStrokeColor(int color) {
            paint.setColor(color);
        }

        public float getStrokeWidth() {
            return paint.getStrokeWidth();
        }

        public void setStrokeWidth(float width) {
            paint.setStrokeWidth(width);
        }
    }
}
