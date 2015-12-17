package pisces.psfoundation.model;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 12/18/15.
 */
public class Resources {
    public static String getString(int resId) {
        return Application.getTopActivity().getString(resId);
    }
}
