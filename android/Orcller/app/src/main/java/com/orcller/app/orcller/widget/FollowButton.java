package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.orcller.app.orcller.R;
import com.orcller.app.orcllermodules.model.BaseUser;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.widget.PSButton;

/**
 * Created by pisces on 12/10/15.
 */
public class FollowButton extends PSButton {
    private BaseUser model;

    public FollowButton(Context context) {
        super(context);
    }

    public FollowButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FollowButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSButton
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        int pd = GraphicUtils.convertDpToPixel(7);

        textView.setTextColor(getResources().getColorStateList(R.drawable.color_button_follow));
        textView.setTextSize(12);
        textView.setPadding(0, pd, 0, pd);
        setBackgroundResource(R.drawable.background_followbutton);
        setDrawableLeft(R.drawable.icon_follow);
        setDrawablePadding(GraphicUtils.convertDpToPixel(5));
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public BaseUser getModel() {
        return model;
    }

    public void setModel(BaseUser model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        setText(getContext().getString(model.isFollowing() ? R.string.w_unfollow : R.string.w_follow));
        setSelected(model.isFollowing());
        setVisibility(model.isMe() ? GONE : VISIBLE);
    }
}
