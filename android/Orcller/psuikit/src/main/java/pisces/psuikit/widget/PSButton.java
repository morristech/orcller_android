package pisces.psuikit.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import pisces.android.R;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 12/1/15.
 */
public class PSButton extends PSFrameLayout {
    private @DrawableRes int drawableLeft;
    private @DrawableRes int drawableTop;
    private @DrawableRes int drawableRight;
    private @DrawableRes int drawableBottom;
    private TextView textView;

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

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PSButton, defStyleAttr, defStyleRes);
        try {
            setDrawableBottom(ta.getResourceId(R.styleable.PSButton_android_drawableBottom, 0));
            setDrawableLeft(ta.getResourceId(R.styleable.PSButton_android_drawableLeft, 0));
            setDrawableRight(ta.getResourceId(R.styleable.PSButton_android_drawableRight, 0));
            setDrawableTop(ta.getResourceId(R.styleable.PSButton_android_drawableTop, 0));
            setDrawablePadding(ta.getDimensionPixelSize(R.styleable.PSButton_android_drawablePadding, 0));
            setText(ta.getString(R.styleable.PSButton_android_text));
            setTextColor(ta.getColorStateList(R.styleable.PSButton_android_textColor));
            setTextSize((float) ta.getDimensionPixelSize(R.styleable.PSButton_android_textSize, (int) getTextSize()));
        } finally {
            ta.recycle();
        }

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        addView(textView, params);
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

    public @DrawableRes int getDrawableLeft() {
        return drawableLeft;
    }

    public void setDrawableLeft(@DrawableRes int drawableLeft) {
        this.drawableLeft = drawableLeft;

        setDrawables();
    }

    public @DrawableRes int getDrawableTop() {
        return drawableTop;
    }

    public void setDrawableTop(@DrawableRes int drawableTop) {
        this.drawableTop = drawableTop;

        setDrawables();
    }

    public @DrawableRes int getDrawableRight() {
        return drawableRight;
    }

    public void setDrawableRight(@DrawableRes int drawableRight) {
        this.drawableRight = drawableRight;

        setDrawables();
    }

    public @DrawableRes int getDrawableBottom() {
        return drawableBottom;
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
        textView.setCompoundDrawablesWithIntrinsicBounds(
                getDrawableLeft(), getDrawableTop(), getDrawableRight(), getDrawableBottom());
    }
}