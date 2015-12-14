package pisces.psfoundation.utils;

import android.content.res.Resources;
import android.view.View;

/**
 * Created by pisces on 11/28/15.
 */
public class GraphicUtils {
    public static int convertDpToPixel(float dp){
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int convertPixelsToDp(float px){
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
