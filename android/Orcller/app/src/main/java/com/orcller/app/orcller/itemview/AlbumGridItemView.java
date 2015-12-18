package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Page;
import com.orcller.app.orcller.widget.PageSelectionIndicatorView;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.ObjectUtils;

/**
 * Created by pisces on 11/29/15.
 */
public class AlbumGridItemView extends ImagePickerMediaItemView {
    private boolean allowsShowCheckIcon;
    private boolean allowsShowDefaultIcon;
    private ImageView defaultIcon;
    private Page page;
    private PageSelectionIndicatorView pageSelectionView;
    private PageSelectionIndicatorView.ItemType itemType;

    public AlbumGridItemView(Context context) {
        super(context);
    }

    public AlbumGridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumGridItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: ImagePickerMediaItemView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        defaultIcon = new ImageView(context);
        defaultIcon.setImageResource(R.drawable.icon_page_default_white);
        defaultIcon.setVisibility(GONE);

        pageSelectionView = new PageSelectionIndicatorView(context);

        LayoutParams params = new LayoutParams(
                GraphicUtils.convertDpToPixel(17), GraphicUtils.convertDpToPixel(17));
        params.bottomMargin = params.rightMargin = GraphicUtils.convertDpToPixel(5);
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;

        addView(defaultIcon, params);
        addView(pageSelectionView);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        pageSelectionView.setVisibility(checked ? VISIBLE : GONE);
        setCheckIconVisibility();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isAllowsShowCheckIcon() {
        return allowsShowCheckIcon;
    }

    public void setAllowsShowCheckIcon(boolean allowsShowCheckIcon) {
        this.allowsShowCheckIcon = allowsShowCheckIcon;
    }

    public boolean isAllowsShowDefaultIcon() {
        return allowsShowDefaultIcon;
    }

    public void setAllowsShowDefaultIcon(boolean allowsShowDefaultIcon) {
        if (allowsShowDefaultIcon == this.allowsShowDefaultIcon)
            return;

        this.allowsShowDefaultIcon = allowsShowDefaultIcon;
        defaultIcon.setVisibility(allowsShowDefaultIcon ? VISIBLE : GONE);
    }

    public PageSelectionIndicatorView.ItemType getItemType() {
        return itemType;
    }

    public void setItemType(PageSelectionIndicatorView.ItemType itemType) {
        if (itemType.equals(this.itemType))
            return;

        this.itemType = itemType;

        pageSelectionView.setItemType(itemType);
        setCheckIconVisibility();
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

    private void setCheckIconVisibility() {
        selectionIndicator.setVisibility(allowsShowCheckIcon &&
                isChecked() &&
                itemType.equals(PageSelectionIndicatorView.ItemType.Right) ? VISIBLE : GONE);
    }
}
