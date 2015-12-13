package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Contributor;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.widget.PSButton;

/**
 * Created by pisces on 12/10/15.
 */
public class CoeditButton extends PSButton implements View.OnClickListener {
    private Album model;

    public CoeditButton(Context context) {
        super(context);
    }

    public CoeditButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoeditButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSButton
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        int pd = GraphicUtils.convertDpToPixel(12);

        textView.setTextColor(getResources().getColorStateList(R.drawable.color_button_follow));
        textView.setTextSize(12);
        setPadding(pd, 0, pd, 0);
        setBackgroundResource(R.drawable.background_ripple_followbutton);
        setOnClickListener(this);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onClick(View v) {
        if (model == null)
            return;

        //TODO: impl accept, ask, cancel, invite
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Album getModel() {
        return model;
    }

    public void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        modelChanged();
    }

    public void reload() {
        modelChanged();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private String getTitle() {
        if (model.contributors.contributor_status == Contributor.Status.Ask.getValue())
            return getContext().getString(R.string.w_cancel);
        if (model.contributors.contributor_status == Contributor.Status.Invite.getValue())
            return getContext().getString(R.string.w_accept);
        return null;
    }

    private void modelChanged() {
        setText(getTitle());
        setVisibility(model.isMine() ||
                model.contributors.contributor_status == Contributor.Status.Accept.getValue() ? GONE : VISIBLE);
    }
}
