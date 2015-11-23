package pisces.psfoundation.utils;

/**
 * Created by pisces on 11/23/15.
 */
public class ObjectUtils {
    public static boolean equals(Object object, Object other) {
        if (object != null && other != null)
            return object.equals(other);
        if (object == null && other == null)
            return true;
        return false;
    }
}
