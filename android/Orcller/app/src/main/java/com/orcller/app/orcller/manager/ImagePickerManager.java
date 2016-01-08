package com.orcller.app.orcller.manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.UserPictureEditActivity;
import com.orcller.app.orcller.activity.imagepicker.FBImagePickerActivity;
import com.orcller.app.orcller.activity.imagepicker.IGImagePickerActivity;
import com.orcller.app.orcller.model.converter.MediaConverter;

import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.event.ImagePickerEvent;
import pisces.psuikit.imagepicker.ImagePickerActivity;

/**
 * Created by pisces on 11/30/15.
 */
public class ImagePickerManager {
    private static ImagePickerManager uniqueInstance;
    private boolean cropEnabled;
    private int choiceMode;
    private Activity context;
    private CompleteHandler completeHandler;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static ImagePickerManager getDefault() {
        if(uniqueInstance == null) {
            synchronized(ImagePickerManager.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new ImagePickerManager();
                }
            }
        }
        return uniqueInstance;
    }

    public void clear() {
        EventBus.getDefault().unregister(this);
        completeHandler = null;
        context = null;
    }

    public void pick(Activity context, CompleteHandler completeHandler) {
        pick(context, AbsListView.CHOICE_MODE_MULTIPLE, false, completeHandler);
    }

    public void pick(Activity context, boolean cropEnabled) {
        pick(context, cropEnabled, null);
    }

    public void pick(Activity context, boolean cropEnabled, CompleteHandler completeHandler) {
        pick(context, cropEnabled ? AbsListView.CHOICE_MODE_SINGLE : AbsListView.CHOICE_MODE_MULTIPLE, cropEnabled, completeHandler);
    }

    public void pick(Activity context, final int choiceMode, boolean cropEnabled, final CompleteHandler completeHandler) {
        clear();

        this.context = context;
        this.choiceMode = choiceMode;
        this.cropEnabled = cropEnabled;
        this.completeHandler = completeHandler;

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                context,
                R.layout.itemview_alertdialog,
                R.id.textView,
                context.getResources().getStringArray(R.array.add_dialog_items));

        new AlertDialog.Builder(context, R.style.CommonAlertDialog)
                .setCancelable(true)
                .setTitle(R.string.w_title_add_dialog)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openImagePicker(which, choiceMode);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        completeHandler.onComplete(null);
                        clear();
                    }
                })
                .create()
                .show();
        EventBus.getDefault().register(this);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onEventMainThread(Object event) {
        if (event instanceof ImagePickerEvent) {
            ImagePickerEvent casted = (ImagePickerEvent) event;

            if (casted.getType().equals(ImagePickerEvent.COMPLETE_SELECTION)) {
                List list = (List) casted.getObject();

                if (cropEnabled && choiceMode == AbsListView.CHOICE_MODE_SINGLE) {
                    UserPictureEditActivity.show(MediaConverter.convert(list.get(0)));
                } else {
                    if (completeHandler != null)
                        completeHandler.onComplete(list);

                    Application.moveToBack(context);
                    clear();
                }
            }
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void openImagePicker(int type, int choiceMode) {
        if (type == 0)
            ImagePickerActivity.show(choiceMode);
        else if (type == 1)
            FBImagePickerActivity.show(choiceMode);
        else if (type == 2)
            IGImagePickerActivity.show(choiceMode);
    }

    // ================================================================================================
    //  Interface: CompleteHandler
    // ================================================================================================

    public static interface CompleteHandler {
        void onComplete(List result);
    }
}
