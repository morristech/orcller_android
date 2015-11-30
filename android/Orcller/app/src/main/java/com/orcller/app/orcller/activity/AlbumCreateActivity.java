package com.orcller.app.orcller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.manager.ImagePickerManager;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.AlbumGridView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;
import com.orcller.app.orcller.widget.UserPictureView;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;

import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSScrollView;
import pisces.psuikit.widget.ClearableEditText;

/**
 * Created by pisces on 11/28/15.
 */
public class AlbumCreateActivity extends PSActionBarActivity
        implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener, AlbumFlipView.Delegate,
        AlbumGridView.Delegate, AdapterView.OnItemSelectedListener {
    private static final String ALBUM_KEY = "album";
    private LinearLayout linearLayout;
    private PSScrollView scrollView;
    private Spinner permissionSpinner;
    private ClearableEditText titleEditText;
    private EditText descriptionEditText;
    private FrameLayout addButton;
    private FrameLayout orderButton;
    private FrameLayout defaultButton;
    private FrameLayout deleteButton;
    private Button postButton;
    private UserPictureView userPictureView;
    private AlbumFlipView albumFlipView;
    private AlbumGridView albumGridView;
    private Album model;
    private Album clonedModel;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_create);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        scrollView = (PSScrollView) findViewById(R.id.scrollView);
        permissionSpinner = (Spinner) findViewById(R.id.permissionSpinner);
        titleEditText = (ClearableEditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        userPictureView = (UserPictureView) findViewById(R.id.userPictureView);
        albumFlipView = (AlbumFlipView) findViewById(R.id.albumFlipView);
        albumGridView = (AlbumGridView) findViewById(R.id.albumGridView);
        addButton = (FrameLayout) findViewById(R.id.addButton);
        orderButton = (FrameLayout) findViewById(R.id.orderButton);
        defaultButton = (FrameLayout) findViewById(R.id.defaultButton);
        deleteButton = (FrameLayout) findViewById(R.id.deleteButton);
        postButton = (Button) findViewById(R.id.postButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.public_options, R.layout.spinner_permission);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.w_new_album));
        permissionSpinner.setAdapter(adapter);
        permissionSpinner.setOnItemSelectedListener(this);
        albumFlipView.setDelegate(this);
        albumGridView.setDelegate(this);
        userPictureView.setModel(AuthenticationCenter.getDefault().getUser());
        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        addButton.setOnClickListener(this);
        orderButton.setOnClickListener(this);
        defaultButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        postButton.setOnClickListener(this);
        EventBus.getDefault().register(this);
        SoftKeyboardNotifier.getDefault().register(this);
    }

    @Override
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            linearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            linearLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        albumFlipView.setPageWidth(linearLayout.getWidth()/2);
        albumFlipView.setPageHeight(linearLayout.getWidth()/2);
        setModel((Album) getIntent().getSerializableExtra(ALBUM_KEY));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        SoftKeyboardNotifier.getDefault().unregister(this);
        permissionSpinner.setOnItemSelectedListener(null);
        addButton.setOnClickListener(null);
        orderButton.setOnClickListener(null);
        defaultButton.setOnClickListener(null);
        deleteButton.setOnClickListener(null);
        postButton.setOnClickListener(null);

        permissionSpinner = null;
        titleEditText = null;
        descriptionEditText = null;
        postButton = null;
        userPictureView = null;
        albumFlipView = null;
        albumGridView = null;
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void startActivity(Album album) {
        Intent intent = new Intent(Application.applicationContext(), AlbumCreateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ALBUM_KEY, album);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * Button listener
     */
    public void onClick(View v) {
        if (v.equals(addButton)) {
            new ImagePickerManager().pick(this, new ImagePickerManager.CompleteHandler() {
                @Override
                public void onComplete(List result) {
                    Log.d("onComplete", result);
                }
            });
        }
    }

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent.Show) {
            if (titleEditText.hasFocus())
                titleEditText.setCursorVisible(true);

            if (descriptionEditText.hasFocus())
                descriptionEditText.setCursorVisible(true);
        } else if (event instanceof SoftKeyboardEvent.Hide) {
            titleEditText.setCursorVisible(false);
            descriptionEditText.setCursorVisible(false);
        }
    }

    /**
     * Spinner listener
     */
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int permission = position + 1;


        if (clonedModel.permission != permission) {
            clonedModel.permission = permission;
            setPostButtonEnabled();
        }

        Log.d("clonedModel.permission", clonedModel.permission, model.permission);
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * AlbumFlipView delegate
     */
    public void onCancelPanning(AlbumFlipView view) {
        scrollView.setScrollable(true);
    }

    public void onChangePageIndex(AlbumFlipView view, int pageIndex) {
        scrollView.setScrollable(true);
        albumGridView.setSelectedIndex(SharedObject.convertPageIndexToPosition(pageIndex));
    }

    public void onLoadRemainPages(AlbumFlipView view) {
    }

    public void onPause(AlbumFlipView view) {
    }

    public void onStartLoadRemainPages(AlbumFlipView view) {
    }

    public void onStartPanning(AlbumFlipView view) {
        scrollView.setScrollable(false);
    }

    public void onStop(AlbumFlipView view) {
    }

    public void onTap(AlbumFlipView view, FlipView flipView, PageView pageView) {
    }

    /**
     * AlbumGridView delegate
     */
    public void onSelect(int position) {
        albumFlipView.setPageIndex(SharedObject.convertPositionToPageIndex(position));
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;

        try {
            clonedModel = (Album) model.clone();
        } catch (CloneNotSupportedException e) {
            if (BuildConfig.DEBUG)
                Log.d(e.getMessage());
        }

        modelChanged();
    }

    private void setPostButtonEnabled() {
        if (model == null || clonedModel == null)
            return;

        postButton.setEnabled(!clonedModel.equalsModel(model));
    }

    private void modelChanged() {
        permissionSpinner.setSelection(clonedModel.permission - 1);
        permissionSpinner.setVisibility(clonedModel.isMine() ? View.VISIBLE : View.GONE);
        titleEditText.setText(clonedModel.name);
        albumFlipView.setModel(clonedModel);
        albumFlipView.setVisibility(clonedModel.pages.count > 0 ? View.VISIBLE : View.GONE);
        albumFlipView.setPageIndex(clonedModel.default_page_index);
        albumGridView.setModel(clonedModel);
        albumGridView.setSelectedIndex(SharedObject.convertPageIndexToPosition(clonedModel.default_page_index));
    }
}