package pisces.psfoundation.model;

import android.graphics.drawable.Drawable;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 12/18/15.
 */
public class Resources {
    public static Drawable getDrawable(int resId) {
        return Application.getTopActivity().getDrawable(resId);
    }

    public static String getString(int resId) {
        return Application.getTopActivity().getString(resId);
    }
}
