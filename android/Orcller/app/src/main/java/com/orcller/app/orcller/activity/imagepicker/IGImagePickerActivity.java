package com.orcller.app.orcller.activity.imagepicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.factory.ExceptionViewFactory;
import com.orcller.app.orcller.itemview.IGImagePickerItemView;
import com.orcller.app.orcllermodules.queue.FBSDKRequestQueue;

import java.util.ArrayList;
import java.util.List;

import pisces.instagram.sdk.InstagramApplicationCenter;
import pisces.instagram.sdk.error.InstagramSDKError;
import pisces.instagram.sdk.model.ApiInstagram;
import pisces.instagram.sdk.proxy.InstagramApiProxy;
import pisces.psfoundation.ext.Application;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.itemview.ListBaseHeaderView;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.ExceptionView;
import retrofit.Call;

/**
 * Created by pisces on 11/27/15.
 */
public class IGImagePickerActivity extends PSActionBarActivity
        implements AdapterView.OnItemClickListener {
    private static final int FOLLOWING_LOAD_LIMIT = 50;
    private int choiceMode;
    private List<ApiInstagram.User> items = new ArrayList<>();
    private Error loadError;
    private FrameLayout container;
    private ListView listView;
    private ListAdapter listAdapter;
    private ApiInstagram.UserListRes lastRes;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_instagram_imagepicker);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.w_instagram_photos));

        container = (FrameLayout) findViewById(R.id.container);
        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new ListAdapter(this);
        choiceMode = getIntent().getIntExtra(MediaGridActivity.CHOICE_MODE_KEY, AbsListView.CHOICE_MODE_MULTIPLE);

        ExceptionView exceptionView = new ExceptionView(this, container);
        exceptionView.setTitleText(R.string.m_exception_title_instagram_login);
        exceptionView.setDescriptionText(R.string.m_exception_desc_instagram_login);
        exceptionView.setButtonText(R.string.m_exception_button_instagram_login);

        exceptionViewManager.add(exceptionView,
                ExceptionViewFactory.create(ExceptionViewFactory.Type.UnknownError, container));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
        load();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FBSDKRequestQueue.currentQueue().onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();
        exceptionViewManager.validate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ProgressBarManager.hide();
        listView.setOnItemClickListener(null);
        InstagramApplicationCenter.getDefault().clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Application.getTopActivity().getMenuInflater().inflate(R.menu.menu_instagram_imagepicker, menu);
        menu.findItem(R.id.popular).setEnabled(InstagramApplicationCenter.getDefault().hasSession());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.popular:
                IGPopularMediaGridActivity.show(choiceMode);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show(int choiceMode) {
        Intent intent = new Intent(Application.applicationContext(), IGImagePickerActivity.class);
        intent.putExtra(MediaGridActivity.CHOICE_MODE_KEY, choiceMode);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view instanceof IGImagePickerItemView)
            IGMediaGridActivity.show(((IGImagePickerItemView) view).getModel(), choiceMode);
    }

    @Override
    public void onClick(ExceptionView view) {
        load();
    }

    @Override
    public boolean shouldShowExceptionView(ExceptionView view) {
        if (ExceptionViewFactory.Type.UnknownError.equals(view.getTag()))
            return loadError != null;
        return !InstagramApplicationCenter.getDefault().hasSession();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void load() {
        if (InstagramApplicationCenter.getDefault().hasSession())
            ProgressBarManager.show();

        loadError = null;
        Call<ApiInstagram.UserRes> call = InstagramApiProxy.getDefault().service().user("self");

        InstagramApplicationCenter.getDefault().enqueueCall(
                call,
                new InstagramApiProxy.CompleteHandler<ApiInstagram.UserRes>() {
                    @Override
                    public void onError(InstagramSDKError error) {
                        loadError = error;
                        ProgressBarManager.hide();
                        exceptionViewManager.validate();
                    }

                    @Override
                    public void onComplete(ApiInstagram.UserRes result) {
                        items.add(result.data);
                        loadFollowing(null);
                    }
                });
    }

    private void loadFollowing(final String after) {
        Call<ApiInstagram.UserListRes> call = InstagramApiProxy.getDefault().service().follows("self", FOLLOWING_LOAD_LIMIT, after);

        InstagramApplicationCenter.getDefault().enqueueCall(
                call,
                new InstagramApiProxy.CompleteHandler<ApiInstagram.UserListRes>() {
                    @Override
                    public void onError(InstagramSDKError error) {
                        loadError = error;
                        ProgressBarManager.hide();
                        exceptionViewManager.validate();
                    }

                    @Override
                    public void onComplete(final ApiInstagram.UserListRes result) {
                        Application.run(new Runnable() {
                            @Override
                            public void run() {
                                lastRes = result;
                                items.addAll(result.data);
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                ProgressBarManager.hide();
                                listAdapter.notifyDataSetChanged();
                                exceptionViewManager.validate();
                            }
                        });
                    }
                });
    }

    // ================================================================================================
    //  Class: ListAdapter
    // ================================================================================================

    private class ListAdapter extends BaseAdapter {
        private static final int HEADER = 1;
        private static final int ITEM = 2;
        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return items.size() > 0 ? items.size() + 2 : items.size();
        }

        @Override
        public ApiInstagram.User getItem(int position) {
            if (getItemViewType(position) == HEADER)
                return null;
            return items.get(getItemPosition(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 || position == 2 ? HEADER : ITEM;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getItemViewType(position) == HEADER) {
                ListBaseHeaderView listBaseHeaderView;

                if (convertView == null) {
                    listBaseHeaderView = new ListBaseHeaderView(context);
                    convertView = listBaseHeaderView;
                } else {
                    listBaseHeaderView = (ListBaseHeaderView) convertView;
                }

                listBaseHeaderView.setText(getHeaderText(position));
            } else {
                IGImagePickerItemView itemView;

                if (convertView == null) {
                    itemView = new IGImagePickerItemView(context);
                    convertView = itemView;
                } else {
                    itemView = (IGImagePickerItemView) convertView;
                }

                int itemPosition = getItemPosition(position);

                itemView.setAllowsShowBackground(itemPosition > 0 && itemPosition < items.size() - 1);
                itemView.setModel(getItem(position));

                if (lastRes != null &&
                        lastRes.getPagination().hasNext() &&
                        position >= items.size() - 5) {
                    loadFollowing(lastRes.pagination.next_max_id);
                }
            }

            return convertView;
        }

        private int getItemPosition(int position) {
            return Math.min(items.size() - 1, Math.max(0, position - 2));
        }

        private String getHeaderText(int position) {
            if (position == 0)
                return getString(R.string.w_me);
            if (position == 2)
                return getString(R.string.w_following);
            return null;
        }
    }
}