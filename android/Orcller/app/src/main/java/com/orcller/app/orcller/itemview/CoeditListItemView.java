package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.text.TextUtils;
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
import com.orcller.app.orcller.utils.CustomSchemeGenerator;
import com.orcller.app.orcller.widget.CoeditButton;

import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;
import pisces.psuikit.ext.PSTextView;

/**
 * Created by pisces on 12/13/15.
 */
public class CoeditListItemView extends PSLinearLayout implements CoeditButton.Delegate {
    private Album model;
    private ImageView imageView;
    private ImageView lockIcon;
    private TextView titleTextView;
    private TextView dateTextView;
    private PSTextView contributorsTextView;
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
        contributorsTextView = (PSTextView) findViewById(R.id.contributorsTextView);
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
        updateContributorsTextView();
    }

    public void onSync(CoeditButton target, Contributors contributors) {
        updateContributorsTextView();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void modelChanged() {
        Glide.with(getContext())
                .load(SharedObject.toFullMediaUrl(model.getCover().media.images.low_resolution.url))
                .into(imageView);
        titleTextView.setText(model.name);
        dateTextView.setText(DateUtil.getRelativeTimeSpanString(model.updated_time));
        lockIcon.setVisibility(model.isMine() ||
                model.permission == Album.Permission.Private.value() ? GONE : VISIBLE);
        updateContributorsTextView();

        try {
            coeditButton.setModel(model);
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.d(e.getMessage(), e);
        }
    }

    private void updateContributorsTextView() {
        contributorsTextView.setText(CustomSchemeGenerator.createContributorsHtml(model.getUser(), model.contributors.data));
        contributorsTextView.setVisibility(TextUtils.isEmpty(contributorsTextView.getText()) ? GONE : VISIBLE);
    }
}
