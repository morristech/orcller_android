package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.api.ApiRelationships;
import com.orcller.app.orcller.proxy.RelationshipsDataProxy;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.BaseUser;
import com.orcller.app.orcllermodules.model.User;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.widget.PSButton;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 12/10/15.
 */
public class FollowButton extends PSButton implements View.OnClickListener {
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
        setBackgroundResource(R.drawable.background_ripple_followbutton);
        setDrawableLeft(R.drawable.icon_follow);
        setDrawablePadding(GraphicUtils.convertDpToPixel(5));
        setOnClickListener(this);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onClick(View v) {
        if (model == null)
            return;

        if (model.isFollowing()) {
            unfollow();
        } else {
            follow();
        }
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

        modelChanged();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void follow() {
        if (invalidDataLoading())
            return;

        RelationshipsDataProxy.getDefault().follow(model.user_uid, getCallback(new Runnable() {
            @Override
            public void run() {
                follow();
            }
        }));
    }

    private Callback<ApiRelationships.FollowRes> getCallback(final Runnable retry) {
        final Runnable error = new Runnable() {
            @Override
            public void run() {
                model.setFollowing(!model.isFollowing());
                modelChanged();
                endDataLoading();
                AlertDialogUtils.retry(R.string.m_fail_common, retry);
            }
        };

        model.setFollowing(!model.isFollowing());
        modelChanged();

        return new Callback<ApiRelationships.FollowRes>() {
            @Override
            public void onResponse(Response<ApiRelationships.FollowRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    endDataLoading();

                    try {
                        User clonedUser = (User) AuthenticationCenter.getDefault().getUser().clone();
                        clonedUser.user_options.follow_count = response.body().entity.follow_count;

                        AuthenticationCenter.getDefault().synchorinzeUser(clonedUser);
                    } catch (CloneNotSupportedException e) {
                        if (BuildConfig.DEBUG)
                            Log.e(e.getMessage(), e);

                        error.run();
                    }
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());

                    error.run();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                error.run();
            }
        };
    }

    private void modelChanged() {
        setText(getContext().getString(model.isFollowing() ? R.string.w_unfollow : R.string.w_follow));
        setSelected(model.isFollowing());
        setVisibility(model.isMe() ? GONE : VISIBLE);
    }

    private void unfollow() {
        if (invalidDataLoading())
            return;

        RelationshipsDataProxy.getDefault().unfollow(model.user_uid, getCallback(new Runnable() {
            @Override
            public void run() {
                unfollow();
            }
        }));
    }
}
