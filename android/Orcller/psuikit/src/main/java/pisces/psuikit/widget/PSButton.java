package pisces.psuikit.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import pisces.android.R;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 12/1/15.
 */
public class PSButton extends PSFrameLayout {
    private @DrawableRes int drawableLeft;
    private @DrawableRes int drawableTop;
    private @DrawableRes int drawableRight;
    private @DrawableRes int drawableBottom;
    private int drawableWidth;
    private int drawableHeight;
    protected TextView textView;

    public PSButton(Context context) {
        super(context);
    }

    public PSButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PSButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        textView = new TextView(context);

        addView(textView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PSButton, defStyleAttr, defStyleRes);
        try {
            drawableWidth = GraphicUtils.convertDpToPixel(ta.getDimension(R.styleable.PSButton_drawableWidth, 0));
            drawableHeight = GraphicUtils.convertDpToPixel(ta.getDimension(R.styleable.PSButton_drawableHeight, 0));

            setDrawableBottom(ta.getResourceId(R.styleable.PSButton_android_drawableBottom, 0));
            setDrawableLeft(ta.getResourceId(R.styleable.PSButton_android_drawableLeft, 0));
            setDrawableRight(ta.getResourceId(R.styleable.PSButton_android_drawableRight, 0));
            setDrawableTop(ta.getResourceId(R.styleable.PSButton_android_drawableTop, 0));
            setDrawablePadding(ta.getDimensionPixelSize(R.styleable.PSButton_android_drawablePadding, 0));
            setText(ta.getString(R.styleable.PSButton_android_text));
            setTextColor(ta.getColorStateList(R.styleable.PSButton_android_textColor));
            setTextLayoutGravity(ta.getInt(R.styleable.PSButton_textLayoutGravity, Gravity.CENTER));
            setTextSize((float) ta.getDimensionPixelSize(R.styleable.PSButton_android_textSize, (int) getTextSize()));
        } finally {
            ta.recycle();
        }

        setClickable(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        textView.setEnabled(enabled);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Drawable getDrawableLeft() {
        return drawableLeft > 0 ? setDrawableSize(getResources().getDrawable(drawableLeft)) : null;
    }

    public void setDrawableLeft(@DrawableRes int drawableLeft) {
        this.drawableLeft = drawableLeft;

        setDrawables();
    }

    public Drawable getDrawableTop() {
        return drawableTop > 0 ? setDrawableSize(getResources().getDrawable(drawableTop)) : null;
    }

    public void setDrawableTop(@DrawableRes int drawableTop) {
        this.drawableTop = drawableTop;

        setDrawables();
    }

    public Drawable getDrawableRight() {
        return drawableRight > 0 ? setDrawableSize(getResources().getDrawable(drawableRight)) : null;
    }

    public void setDrawableRight(@DrawableRes int drawableRight) {
        this.drawableRight = drawableRight;

        setDrawables();
    }

    public Drawable getDrawableBottom() {
        return drawableBottom > 0 ? setDrawableSize(getResources().getDrawable(drawableBottom)) : null;
    }

    public void setDrawableBottom(@DrawableRes int drawableBottom) {
        this.drawableBottom = drawableBottom;

        setDrawables();
    }

    public int getDrawablePadding() {
        return textView.getCompoundDrawablePadding();
    }

    public void setDrawablePadding(int drawablePadding) {
        textView.setCompoundDrawablePadding(drawablePadding);
    }

    public void setDrawableBound(Rect rect) {
        setDrawableSize(rect.width(), rect.height());
    }

    public void setDrawableSize(int width, int height) {
        drawableWidth = width;
        drawableHeight = height;

        setDrawables();
    }

    public int getTextLayoutGravity() {
        return ((LayoutParams) textView.getLayoutParams()).gravity;
    }

    public void setTextLayoutGravity(int gravity) {
        ((LayoutParams) textView.getLayoutParams()).gravity = gravity;
    }

    public CharSequence getText() {
        return textView.getText();
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public ColorStateList getTextColor() {
        return textView.getTextColors();
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

    private void setDrawables() {
        if (drawableWidth <= 0 || drawableHeight <= 0)
            textView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
        else
            textView.setCompoundDrawables(getDrawableLeft(), getDrawableTop(), getDrawableRight(), getDrawableBottom());
    }

    private Drawable setDrawableSize(Drawable drawable) {
        if (drawable != null && drawableWidth > 0 && drawableHeight > 0)
            drawable.setBounds(0, 0, drawableWidth, drawableHeight);
        return drawable;
    }
}
