package com.orcller.app.orcller.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Max;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.event.PageListEvent;
import com.orcller.app.orcller.manager.ImagePickerManager;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.manager.MediaUploadUnit;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.album.Page;
import com.orcller.app.orcller.model.converter.MediaConverter;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.AlbumGridView;
import com.orcller.app.orcller.widget.DescriptionInputView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;
import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;
import com.orcller.app.orcllermodules.utils.SoftKeyboardNotifier;
import com.orcller.app.orcllermodules.utils.SoftKeyboardUtils;

import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.ext.PSScrollView;
import pisces.psuikit.widget.ClearableEditText;
import pisces.psuikit.widget.PSButton;

/**
 * Created by pisces on 11/28/15.
 */
public class AlbumCreateActivity extends PSActionBarActivity
        implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener, AlbumFlipView.Delegate,
        AlbumGridView.Delegate, AdapterView.OnItemSelectedListener {
    private int selectedIndexForAppending;
    private int processCountForAppending;
    private Validator validator;
    private LinearLayout rootLayout;
    private PSScrollView scrollView;
    private FrameLayout spinnerContainer;
    private LinearLayout buttonContainer;
    private Spinner spinner;

    @Max(value = 40)
    private ClearableEditText titleEditText;

    private Button postButton;
    private PSButton addButton;
    private PSButton orderButton;
    private PSButton defaultButton;
    private PSButton deleteButton;
    private DescriptionInputView descriptionInputView;
    private AlbumFlipView albumFlipView;
    private AlbumGridView albumGridView;
    private Album model;
    protected Album clonedModel;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_create);

        rootLayout = (LinearLayout) findViewById(R.id.linearLayout);
        spinnerContainer = (FrameLayout) findViewById(R.id.spinnerContainer);
        spinner = (Spinner) findViewById(R.id.spinner);
        scrollView = (PSScrollView) findViewById(R.id.scrollView);
        titleEditText = (ClearableEditText) findViewById(R.id.titleEditText);
        descriptionInputView = (DescriptionInputView) findViewById(R.id.descriptionInputView);
        albumFlipView = (AlbumFlipView) findViewById(R.id.albumFlipView);
        albumGridView = (AlbumGridView) findViewById(R.id.albumGridView);
        buttonContainer = (LinearLayout) findViewById(R.id.buttonContainer);
        addButton = (PSButton) findViewById(R.id.addButton);
        orderButton = (PSButton) findViewById(R.id.orderButton);
        defaultButton = (PSButton) findViewById(R.id.defaultButton);
        deleteButton = (PSButton) findViewById(R.id.deleteButton);
        postButton = (Button) findViewById(R.id.postButton);
        validator = new Validator(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.public_options, R.layout.spinner_permission);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.w_title_new_album));
        spinner.setAdapter(adapter);
        albumFlipView.setAllowsShowPageCount(false);
        setListeners();
    }

    @Override
    public void onGlobalLayout() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        albumFlipView.setPageWidth(rootLayout.getWidth() / 2);
        albumFlipView.setPageHeight(rootLayout.getWidth() / 2);

        setModel(createModel());
    }

    @Override
    protected void onResume() {
        super.onResume();

        ImagePickerManager.getDefault().clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        SoftKeyboardNotifier.getDefault().unregister(this);
        spinner.setOnItemSelectedListener(null);
        addButton.setOnClickListener(null);
        orderButton.setOnClickListener(null);
        defaultButton.setOnClickListener(null);
        deleteButton.setOnClickListener(null);
        postButton.setOnClickListener(null);

        spinnerContainer = null;
        spinner = null;
        titleEditText = null;
        descriptionInputView = null;
        postButton = null;
        albumFlipView = null;
        albumGridView = null;
        validator = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (postButton.isEnabled()) {
                    showCloseAlert();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (postButton.isEnabled()) {
                    showCloseAlert();
                    return true;
                }

                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void show() {
        Intent intent = new Intent(Application.applicationContext(), AlbumCreateActivity.class);
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
            ImagePickerManager.getDefault().pick(this, new ImagePickerManager.CompleteHandler() {
                @Override
                public void onComplete(List result) {
                    appendPages(result);
                }
            });
        } else if (v.equals(orderButton)) {
            AlbumPageOrderActivity.show(clonedModel);
        } else if (v.equals(defaultButton)) {
            AlbumPageDefaultActivity.show(clonedModel);
        } else if (v.equals(deleteButton)) {
            AlbumPageDeleteActivity.show(clonedModel);
        } else if (v.equals(postButton)) {
            validator.validate();
        }
    }

    /**
     * EventBus listener
     */
    public void onEventMainThread(final Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                if (titleEditText.hasFocus())
                    titleEditText.setCursorVisible(true);

                buttonContainer.setVisibility(View.GONE);
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                titleEditText.setCursorVisible(false);
                buttonContainer.setVisibility(View.VISIBLE);
            }
        } else if (event instanceof PageListEvent) {
            PageListEvent casted = (PageListEvent) event;
            final Album model = (Album) casted.getObject();

            if (casted.getType() == PageListEvent.PAGE_EDIT_COMPLETE) {
                Application.run(new Runnable() {
                    @Override
                    public void run() {
                        clonedModel.removeAllPages();

                        for (Page page : model.pages.data) {
                            clonedModel.addPage(page);
                        }
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        selectedIndexForAppending = 0;
                        reload();
                        setPostButtonEnabled();
                        setOtherButtonsEnabled();
                    }
                });
            } else if (casted.getType() == PageListEvent.PAGE_DEFAULT_CHANGE_COMPLETE) {
                clonedModel.default_page_index = model.default_page_index;
                selectedIndexForAppending = SharedObject.convertPageIndexToPosition(model.default_page_index);
                reload();
                setPostButtonEnabled();
            }
        }
    }

    /**
     * Spinner listener
     */
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (clonedModel == null)
            return;

        int permission = position + 1;
        if (clonedModel.permission != permission) {
            clonedModel.permission = permission;
            setPostButtonEnabled();
        }
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
    //  Protected
    // ================================================================================================

    protected Album createModel() {
        return new Album(AuthenticationCenter.getDefault().getUser());
    }

    protected void doRequest() {
        MediaManager.getDefault().getUnit(clonedModel).setCompletionState(MediaUploadUnit.CompletionState.Creation);
        MediaManager.getDefault().startUploading(clonedModel);
    }

    protected void setModel(Album model) {
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

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void appendPages(final List list) {
        selectedIndexForAppending = clonedModel.pages.data.size();
        processCountForAppending = 0;

        final AppendPage appendPage = new AppendPage() {
            @Override
            public void append(Page page) {
                clonedModel.addPage(page);

                Application.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        reload();

                        if (++processCountForAppending >= list.size()) {
                            setPostButtonEnabled();
                            setOtherButtonsEnabled();
                            MediaManager.getDefault().startUploading(clonedModel);
                        }
                    }
                });
            }
        };

        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                for (Object object : list) {
                    if (object instanceof Media) {
                        appendPage.append(Page.create((Media) object));
                    } else if (object instanceof pisces.psuikit.imagepicker.Media) {
                        final Media media = MediaConverter.convert(object);

                        MediaManager.getDefault().saveToTemp(
                                (pisces.psuikit.imagepicker.Media) object,
                                media, new MediaManager.CompleteHandler() {
                                    @Override
                                    public void onComplete(Error error) {
                                        appendPage.append(Page.create(media));
                                    }
                                });
                    }
                }
            }
        });
    }

    private void modelChanged() {
        spinner.setSelection(clonedModel.permission - 1);
        spinnerContainer.setVisibility(clonedModel.isMine() ? View.VISIBLE : View.GONE);
        titleEditText.setEnabled(clonedModel.isMine());
        titleEditText.setText(clonedModel.name);
        descriptionInputView.setVisibility(clonedModel.isMine() ? View.VISIBLE : View.GONE);
        descriptionInputView.setText(clonedModel.desc);
        descriptionInputView.setModel(clonedModel.getUser());
        albumFlipView.setModel(clonedModel);
        albumFlipView.setPageIndex(clonedModel.default_page_index);
        albumFlipView.setVisibility(clonedModel.pages.count > 0 ? View.VISIBLE : View.GONE);
        albumGridView.setModel(clonedModel);
        albumGridView.setSelectedIndex(SharedObject.convertPageIndexToPosition(clonedModel.default_page_index));
        scrollView.setBackgroundResource(spinnerContainer.isShown() ? 0 : R.drawable.background_bordered_lightgray);
        scrollView.setVisibility(View.VISIBLE);
        buttonContainer.setVisibility(View.VISIBLE);
        setOtherButtonsEnabled();

        if (clonedModel.pages.data.size() < 1)
            onClick(addButton);
    }

    private void reload() {
        albumFlipView.reload();
        albumGridView.reload();
        albumFlipView.setPageIndex(SharedObject.convertPositionToPageIndex(selectedIndexForAppending));
        albumFlipView.setVisibility(clonedModel.pages.count > 0 ? View.VISIBLE : View.GONE);
        albumGridView.setSelectedIndex(selectedIndexForAppending);
    }

    private void setListeners() {
        spinner.setOnItemSelectedListener(this);
        albumFlipView.setDelegate(this);
        albumGridView.setDelegate(this);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        addButton.setOnClickListener(this);
        orderButton.setOnClickListener(this);
        defaultButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        postButton.setOnClickListener(this);
        EventBus.getDefault().register(this);
        SoftKeyboardNotifier.getDefault().register(this);

        titleEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CharSequence charSequence = titleEditText.getText();

                if (clonedModel != null && !TextUtils.isEmpty(clonedModel.name) && !TextUtils.isEmpty(charSequence)) {
                    clonedModel.name = TextUtils.isEmpty(charSequence) ? null : charSequence.toString();
                    setPostButtonEnabled();
                }
            }
        });

        descriptionInputView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CharSequence charSequence = descriptionInputView.getText();

                if (clonedModel != null) {
                    clonedModel.desc = TextUtils.isEmpty(charSequence) ? null : charSequence.toString();
                    setPostButtonEnabled();
                }
            }
        });

        validator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                clonedModel.created_time = clonedModel.updated_time = DateUtil.toUnixtimestamp(new Date());
                clonedModel.page_replace_enabled = !clonedModel.pages.data.equals(model.pages.data);
                postButton.setEnabled(false);
                SoftKeyboardUtils.hide(rootLayout);
                doRequest();
                finish();
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {
                for (ValidationError error : errors) {
                    View view = error.getView();
                    String message = error.getCollatedErrorMessage(getBaseContext());
                    if (view instanceof EditText) {
                        ((EditText) view).setError(message);
                    } else {
                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void setOtherButtonsEnabled() {
        if (clonedModel != null) {
            orderButton.setEnabled(clonedModel.pages.data.size() > 1);
            defaultButton.setEnabled(clonedModel.isMine() && clonedModel.pages.data.size() > 1);
            deleteButton.setEnabled(clonedModel.pages.data.size() > 0);
        }
    }

    private void setPostButtonEnabled() {
        if (model == null || clonedModel == null)
            return;

        model.equalsModel(clonedModel, new Model.EqualsCompletion() {
            @Override
            public void onComplete(boolean equals) {
                postButton.setEnabled(!equals);
            }
        });
    }

    private void showCloseAlert() {
        AlertDialogUtils.show(getString(R.string.m_activity_close_message),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == AlertDialog.BUTTON_POSITIVE) {
                            onBackPressed();
                        }
                    }
                },
                getString(R.string.w_no),
                getString(R.string.w_yes)
        );
    }

    private interface AppendPage {
        void append(Page page);
    }
}