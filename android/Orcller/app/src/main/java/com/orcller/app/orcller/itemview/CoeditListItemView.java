package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Contributors;
import com.orcller.app.orcller.widget.CoeditButton;

import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;

/**
 * Created by pisces on 12/13/15.
 */
public class CoeditListItemView extends PSLinearLayout implements CoeditButton.Delegate {
    private Album model;
    private ImageView imageView;
    private ImageView lockIcon;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView contributorsTextView;
    private CoeditButton coeditButton;

    public CoeditListItemView(Context context) {
        super(context);
    }

    public CoeditListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoeditListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_coedit, this);

        imageView = (ImageView) findViewById(R.id.imageView);
        lockIcon = (ImageView) findViewById(R.id.lockIcon);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        contributorsTextView = (TextView) findViewById(R.id.contributorsTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        coeditButton = (CoeditButton) findViewById(R.id.coeditButton);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coeditButton.setDelegate(this);
        contributorsTextView.setMovementMethod(LinkMovementMethod.getInstance());
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

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * CoeditButton.Delegate
     */
    public void onChange(CoeditButton target, Contributors contributors) {
        updateDisplayList();
    }

    public void onSync(CoeditButton target, Contributors contributors) {
        updateDisplayList();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void modelChanged() {
        Glide.clear(imageView);
        Glide.with(getContext())
                .load(SharedObject.toFullMediaUrl(model.getCover().media.images.low_resolution.url))
                .error(R.drawable.img_fb_empty_album)
                .into(imageView);

        try {
            coeditButton.setModel(model);
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.d(e.getMessage(), e);
        }

        updateDisplayList();
    }

    private void updateContributorsTextView() {
        String text = model.contributors.total_count > 0 ?
                String.valueOf(model.contributors.total_count) + Resources.getString(R.string.w_people_involved_in) :
                Resources.getString(R.string.w_no_contributors);
        contributorsTextView.setText(text);
    }

    private void updateDisplayList() {
        titleTextView.setText(model.name);
        dateTextView.setText(DateUtil.getRelativeTimeSpanString(model.updated_time));
        lockIcon.setVisibility(model.isMine() || model.permission != Album.Permission.Private.value() ? GONE : VISIBLE);
        updateContributorsTextView();
    }
}
