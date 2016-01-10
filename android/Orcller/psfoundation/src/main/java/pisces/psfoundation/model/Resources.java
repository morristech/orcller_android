package pisces.psfoundation.model;

import android.graphics.drawable.Drawable;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 12/18/15.
 */
public class Resources {
    public static int getColor(int resId) {
        return Application.getTopActivity().getResources().getColor(resId);
    }

    public static Drawable getDrawable(int resId) {
        return Application.getTopActivity().getResources().getDrawable(resId);
    }

    public static String getString(int resId) {
        return Application.getTopActivity().getResources().getString(resId);
    }
}
