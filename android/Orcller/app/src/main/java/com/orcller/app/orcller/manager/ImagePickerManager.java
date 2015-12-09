package com.orcller.app.orcller.manager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.widget.ArrayAdapter;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.imagepicker.FBImagePickerActivity;
import com.orcller.app.orcller.activity.imagepicker.IGImagePickerActivity;

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
    private Context context;
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

    public void pick(Context context, CompleteHandler completeHandler) {
        clear();

        this.context = context;
        this.completeHandler = completeHandler;

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                context,
                R.layout.itemview_alertdialog,
                R.id.textView,
                context.getResources().getStringArray(R.array.add_dialog_items));

        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.WhiteThemeAlertDialog))
                .setCancelable(true)
                .setTitle(R.string.w_add_dialog_title)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openImagePicker(which);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
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
                completeHandler.onComplete((List) casted.getObject());
                clear();
            }
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void openImagePicker(int type) {
        Class activityClass = null;
        if (type == 0)
            activityClass = ImagePickerActivity.class;
        else if (type == 1)
            activityClass = FBImagePickerActivity.class;
        else if (type == 2)
            activityClass = IGImagePickerActivity.class;

        Intent intent = new Intent(context, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Application.getTopActivity().startActivity(intent);
    }

    // ================================================================================================
    //  Interface: CompleteHandler
    // ================================================================================================

    public static interface CompleteHandler {
        void onComplete(List result);
    }
}
