package com.orcller.app.orcller.activity.imagepicker;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.SwipeRefreshLayout;

import com.orcller.app.orcller.R;

import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.instagram.sdk.error.InstagramSDKError;
import pisces.instagram.sdk.model.ApiInstagram;
import pisces.instagram.sdk.proxy.InstagramApiProxy;
import pisces.psuikit.manager.ProgressBarManager;
import retrofit.Call;

/**
 * Created by pisces on 11/28/15.
 */
public class IGPopularMediaGridActivity extends MediaGridActivity
        implements SwipeRefreshLayout.OnRefreshListener {
    private static final int LOAD_LIMIT = 50;
    private ApiInstagram.MediaListRes lastRes;
    private SwipeRefreshLayout swipeRefreshLayout;

    // ================================================================================================
    //  Overridden: MediaGridActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.w_popular));

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected @LayoutRes
    int getLayoutRes() {
        return R.layout.activity_instagram_popular_mediagrid;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        swipeRefreshLayout.setRefreshing(false);
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
    //  Listener
    // ================================================================================================

    @Override
    public void onRefresh() {
        load(null);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load(final String after) {
        if (invalidDataLoading())
            return;

        if (after == null && isFirstLoading())
            ProgressBarManager.show(this);

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