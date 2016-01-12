package pisces.psuikit.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;

import pisces.android.R;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Resources;

/**
 * Created by pisces on 11/11/15.
 */
public class AlertDialogUtils {
    public static void show(@StringRes int message, int... buttons) {
        show(Resources.getString(message), null, buttons);
    }

    public static void show(@StringRes int message, DialogInterface.OnClickListener listener, int... buttons) {
        show(Resources.getString(message), listener, buttons);
    }

    public static void show(String message, String... buttons) {
        show(message, null, buttons);
    }

    public static void show(String message, DialogInterface.OnClickListener listener, int... buttons) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Application.getTopActivity());
        alertDialog.setMessage(message);

        for (int i=0; i<buttons.length; i++) {
            int labelId = buttons[i];
            if (i == 0)
                alertDialog.setNegativeButton(Resources.getString(labelId), listener);
            else
                alertDialog.setPositiveButton(Resources.getString(labelId), listener);
        }

        alertDialog.show();
    }

    public static void show(String message, DialogInterface.OnClickListener listener, String... buttons) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Application.getTopActivity());
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

    public static void retry(int message, Runnable retry) {
        retry(Resources.getString(message), retry);
    }

    public static void retry(int message, DialogInterface.OnClickListener listener) {
        Context context = Application.getTopActivity();
        show(context.getString(message), listener, context.getString(R.string.w_dismiss), context.getString(R.string.w_retry));
    }

    public static void retry(String message, DialogInterface.OnClickListener listener) {
        Context context = Application.getTopActivity();
        show(message, listener,context.getString(R.string.w_dismiss), context.getString(R.string.w_retry));
    }

    public static void retry(String message, final Runnable retry) {
        Context context = Application.getTopActivity();
        show(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == AlertDialog.BUTTON_POSITIVE) {
                            retry.run();
                        }
                    }
                },
                context.getString(R.string.w_dismiss),
                context.getString(R.string.w_retry));
    }
}
