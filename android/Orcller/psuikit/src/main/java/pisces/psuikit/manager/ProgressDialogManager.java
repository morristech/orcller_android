package pisces.psuikit.manager;

import android.app.ProgressDialog;
import android.content.Context;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Resources;

/**
 * Created by pisces on 11/9/15.
 */
public class ProgressDialogManager {
    private static boolean showing;
    private static ProgressDialog progressDialog;
    public static void hide() {
        if (!showing)
            return;

        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog = null;
        }
    }

    public static boolean isShowing() {
        return showing;
    }

    public static void show(Context context, int messageResId) {
        show(context, Resources.getString(messageResId));
    }

    public static void show(int messageResId) {
        show(Resources.getString(messageResId));
    }

    public static void show(String message) {
        show(Application.getTopActivity(), message);
    }

    public static void show(Context context, String message) {
        if (showing)
            hide();

        progressDialog = ProgressDialog.show(context, null, message);
        showing = true;
    }
}
