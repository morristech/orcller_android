package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.util.AttributeSet;

import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/11/15.
 */
abstract public class AbstractDataGridItemView extends PSLinearLayout {
    private Model model;

    public AbstractDataGridItemView(Context context) {
        super(context);
    }

    public AbstractDataGridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractDataGridItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, getLayoutRes(), this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    /**
     * @abstract
     */
    abstract protected int getLayoutRes();

    /**
     * @abstract
     */
    abstract protected void modelChanged();
}
