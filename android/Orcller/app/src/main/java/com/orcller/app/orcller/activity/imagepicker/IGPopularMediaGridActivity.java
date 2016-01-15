package com.orcller.app.orcller.activity.imagepicker;

import android.content.Intent;
import android.os.Bundle;

import com.orcller.app.orcller.R;

import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.instagram.sdk.error.InstagramSDKError;
import pisces.instagram.sdk.model.ApiInstagram;
import pisces.instagram.sdk.proxy.InstagramApiProxy;
import pisces.psfoundation.ext.Application;
import retrofit.Call;

/**
 * Created by pisces on 11/28/15.
 */
public class IGPopularMediaGridActivity extends MediaGridActivity {
    private static final int LOAD_LIMIT = 50;
    private ApiInstagram.MediaListRes lastRes;

    // ================================================================================================
    //  Overridden: MediaGridActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.w_popular));
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

    public static void show(int choiceMode) {
        Intent intent = new Intent(Application.applicationContext(), IGPopularMediaGridActivity.class);
        intent.putExtra(CHOICE_MODE_KEY, choiceMode);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(final String after) {
        if (invalidDataLoading(after))
            return;

        Call<ApiInstagram.MediaListRes> call = InstagramApiProxy.getDefault().
                service().popularMedia(LOAD_LIMIT, after);

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