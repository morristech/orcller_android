package pisces.psuikit.ext;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by pisces on 11/27/15.
 */
public interface PSComponent {
    boolean isImmediatelyUpdating();
    void setImmediatelyUpdating(boolean immediatelyUpdating);
    void invalidateProperties();
    void validateProperties();
}
