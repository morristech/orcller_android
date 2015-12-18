package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Page;

import pisces.psfoundation.utils.ObjectUtils;

/**
 * Created by pisces on 12/1/15.
 */
public class AlbumPageGridItemView extends ImagePickerMediaItemView {
    private boolean allowsShowDefaultIcon;
    private ImageView defaultIcon;
    private TextView textView;
    private Page page;

    public AlbumPageGridItemView(Context context) {
        super(context);
    }

    public AlbumPageGridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumPageGridItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: ImagePickerMediaItemView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        defaultIcon = (ImageView) findViewById(R.id.defaultIcon);
        textView = (TextView) findViewById(R.id.textView);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.itemview_album_pagegrid;
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        setEnabledState();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        setEnabledState();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isAllowsShowDefaultIcon() {
        return allowsShowDefaultIcon;
    }

    public void setAllowsShowDefaultIcon(boolean allowsShowDefaultIcon) {
        if (allowsShowDefaultIcon == this.allowsShowDefaultIcon)
            return;

        this.allowsShowDefaultIcon = allowsShowDefaultIcon;
        defaultIcon.setVisibility(allowsShowDefaultIcon ? VISIBLE : GONE);
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        if (ObjectUtils.equals(page, this.page))
            return;

        this.page = page;

        setModel(page.media);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setEnabledState() {
        if (!isEnabled())
            selectionIndicator.setVisibility(VISIBLE);

        checkIcon.setVisibility(isEnabled() ? VISIBLE : INVISIBLE);
    }
}
