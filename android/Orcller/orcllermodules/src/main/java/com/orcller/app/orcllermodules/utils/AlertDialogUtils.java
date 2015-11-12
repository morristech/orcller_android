package com.orcller.app.orcllermodules.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.orcller.app.orcllermodules.ext.Application;

/**
 * Created by pisces on 11/11/15.
 */
public class AlertDialogUtils {
    public static void show(Context context, String message, String... buttons) {
        show(context, message, null, buttons);
    }

    public static void show(Context context, String message, DialogInterface.OnClickListener listener, String... buttons) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage(message);

        for (int i=0; i<buttons.length; i++) {
            String label = buttons[i];
            if (i == 0)
                alertDialog.setNegativeButton(label, listener);
            else
                alertDialog.setPositiveButton(label, listener);
        }

        alertDialog.show();
    }
}
