package com.orcller.app.orcller.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.itemview.IdpListItemView;
import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.api.Api;
import com.orcller.app.orcllermodules.model.facebook.FBUser;
import com.orcller.app.orcllermodules.queue.FBSDKRequest;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.instagram.sdk.error.InstagramSDKError;
import pisces.instagram.sdk.model.ApiInstagram;
import pisces.instagram.sdk.model.ApiInstagramResult;
import pisces.instagram.sdk.proxy.InstagramApiProxy;
import pisces.psuikit.ext.PSFrameLayout;
import pisces.psuikit.itemview.ListEmptyHeaderView;
import pisces.psuikit.widget.SectionedListView;
import retrofit.Call;

/**
 * Created by pisces on 12/24/15.
 */
public class ConnectedAccountsAlertDialogView extends PSFrameLayout
        implements SectionedListView.DataSource, SectionedListView.Delegate {
    private SectionedListView listView;
    private String facebookUserName;
    private String instagramUserName;

    public ConnectedAccountsAlertDialogView(Context context) {
        super(context);
    }

    public ConnectedAccountsAlertDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConnectedAccountsAlertDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSFrameLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_alertdialog_sectioned_list, this);

        listView = (SectionedListView) findViewById(R.id.listView);

        listView.setDataSource(this);
        listView.setDelegate(this);
        listView.reload();

        if (FBSDKRequestQueue.currentQueue().isValidAccessToken())
            loadFacebookProfile();

        loadInstagramProfile();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * SectionedListView.DataSource
     */
    public View getHeaderView(ListView listView, View convertView, int section) {
        if (convertView == null)
            convertView = new ListEmptyHeaderView(getContext());
        return convertView;
    }

    public View getItemView(ListView listView, View convertView, SectionedListView.IndexPath indexPath) {
        if (convertView == null) {
            convertView = new IdpListItemView(getContext());
        }

        if (convertView instanceof IdpListItemView) {
            IdpListItemView view = (IdpListItemView) convertView;

            view.setImageDrawable(getResources().getDrawable(getImageDrawableResId(indexPath)));
            view.setTitleText(getTitleTextResId(indexPath));
            view.setDetailText(getDescriptionText(indexPath));
        }

        return convertView;
    }

    public int getRowCount(ListView listView, int section) {
        return 2;
    }

    public int getSectionCount(ListView listView) {
        return 1;
    }

    public int getItemViewType(ListView listView, SectionedListView.IndexPath indexPath) {
        return 1;
    }

    public int getItemViewTypeCount(ListView listView) {
        return 1;
    }

    /**
     * SectionedListView.Delegate
     */
    public boolean isEnabled(ListView listView, SectionedListView.IndexPath indexPath) {
        return true;
    }

    public void onItemClick(ListView listView, View view, SectionedListView.IndexPath indexPath) {
        if (indexPath.row() == 0)
            loginFacebook();
        else
            loginInstagram();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private String getDescriptionText(SectionedListView.IndexPath indexPath) {
        return indexPath.row() == 0 ? facebookUserName : instagramUserName;
    }

    private int getImageDrawableResId(SectionedListView.IndexPath indexPath) {
        return indexPath.row() == 0 ? R.drawable.icon_facebook_normal : R.drawable.icon_instagram_normal;
    }

    private int getTitleTextResId(SectionedListView.IndexPath indexPath) {
        return indexPath.row() == 0 ? R.string.w_facebook : R.string.w_instagram;
    }

    private void loadFacebookProfile() {
        if (facebookUserName != null)
            return;

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name");

        FBSDKRequestQueue.currentQueue().request(
                this,
                "me",
                parameters,
                FBUser.class,
                new FBSDKRequest.CompleteHandler<FBUser>() {
                    @Override
                    public void onComplete(final FBUser result, APIError error) {
                        if (error == null) {
                            facebookUserName = result.name;
                            listView.reload();
                        }
                    }
                });
    }

    private void loadInstagramProfile() {
        if (!InstagramApplicationCenter.getDefault().hasSession() || instagramUserName != null)
            return;

        Call<ApiInstagram.UserRes> call = InstagramApiProxy.getDefault().service().user("self");

        InstagramApplicationCenter.getDefault().enqueueCall(
                call,
                new InstagramApiProxy.CompleteHandler<ApiInstagram.UserRes>() {
                    @Override
                    public void onError(InstagramSDKError error) {
                    }

                    @Override
                    public void onComplete(ApiInstagram.UserRes result) {
                        instagramUserName = result.data.username;
                        listView.reload();
                    }
                });
    }

    private void loginFacebook() {
        if (FBSDKRequestQueue.currentQueue().isValidAccessToken())
            return;

        AuthenticationCenter.getDefault().syncWithFacebook(this, new Api.CompleteHandler() {
            @Override
            public void onComplete(Object result, APIError error) {
                if (error == null) {
                    facebookUserName = ((FBUser) result).name;
                    listView.reload();
                }
            }
        });
    }

    private void loginInstagram() {
        if (InstagramApplicationCenter.getDefault().hasSession())
            return;

        InstagramApplicationCenter.getDefault().login(new InstagramApiProxy.CompleteHandler() {
            @Override
            public void onError(InstagramSDKError error) {
            }

            @Override
            public void onComplete(ApiInstagramResult result) {
                loadInstagramProfile();
            }
        });
    }
}
