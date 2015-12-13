package com.orcller.app.orcller.activity.imagepicker;

import android.content.Intent;
import android.os.Bundle;

import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.instagram.sdk.error.InstagramSDKError;
import pisces.instagram.sdk.model.ApiInstagram;
import pisces.instagram.sdk.proxy.InstagramApiProxy;
import pisces.psfoundation.ext.Application;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Call;

/**
 * Created by pisces on 11/27/15.
 */
public class IGMediaGridActivity extends MediaGridActivity {
    private static final int LOAD_LIMIT = 50;
    public static final String USER_KEY = "user";
    private ApiInstagram.User user;
    private ApiInstagram.MediaListRes lastRes;

    // ================================================================================================
    //  Overridden: MediaGridActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = (ApiInstagram.User) getIntent().getSerializableExtra(USER_KEY);

        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(user.username);
    }

    @Override
    protected void loadContent() {
        load(null);
    }

    @Override
    protected void loadMore(int position) {
        if (lastRes != null && lastRes.getPagination().hasNext() && position >= items.size() - 9)
            load(lastRes.pagination.next_max_id);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(ApiInstagram.User user, int choiceMode) {
        Intent intent = new Intent(Application.applicationContext(), IGMediaGridActivity.class);
        intent.putExtra(CHOICE_MODE_KEY, choiceMode);
        intent.putExtra(USER_KEY, user);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(final String after) {
        if (user == null)
            return;

        if (after == null)
            ProgressBarManager.show(this);

        Call<ApiInstagram.MediaListRes> call = InstagramApiProxy.getDefault().
                service().recentMedia(user.id, LOAD_LIMIT, after);

        InstagramApplicationCenter.getDefault().enqueueCall(
                call,
                new InstagramApiProxy.CompleteHandler<ApiInstagram.MediaListRes>() {
                    @Override
                    public void onError(InstagramSDKError error) {
                        loadComplete(null, error, after == null);
                    }

                    @Override
                    public void onComplete(ApiInstagram.MediaListRes result) {
                        lastRes = result;
                        loadComplete(result.data, null, after == null);
                    }
                });
    }
}
