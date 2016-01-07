package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.widget.MediaThumbView;

import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSFrameLayout;

/**
 * Created by pisces on 11/26/15.
 */
public class ImagePickerMediaItemView extends PSFrameLayout implements Checkable {
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private boolean allowsShowIndicator = true;
    private boolean checked;
    private Media model;
    private MediaThumbView mediaThumbView;
    protected ImageView checkIcon;
    protected FrameLayout selectionIndicator;

    public ImagePickerMediaItemView(Context context) {
        super(context);
    }

    public ImagePickerMediaItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImagePickerMediaItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, getLayoutRes(), this);

        mediaThumbView = (MediaThumbView) findViewById(R.id.mediaThumbView);
        checkIcon = (ImageView) findViewById(R.id.checkIcon);
        selectionIndicator = (FrameLayout) findViewById(R.id.selectionIndicator);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isAllowsShowIndicator() {
        return allowsShowIndicator;
    }

    public void setAllowsShowIndicator(boolean allowsShowIndicator) {
        this.allowsShowIndicator = allowsShowIndicator;
    }

    public Media getModel() {
        return model;
    }

    public void setModel(Media model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    // ================================================================================================
    //  Impl: Checkable
    // ================================================================================================

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked())
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked == this.checked)
            return;;

        this.checked = checked;

        refreshDrawableState();
        selectionIndicator.setVisibility(allowsShowIndicator && checked ? VISIBLE : INVISIBLE);
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected int getLayoutRes() {
        return R.layout.itemview_media_imagepicker;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void modelChanged() {
        mediaThumbView.setModel(model);
    }
}