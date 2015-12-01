package pisces.psfoundation.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by pisces on 11/23/15.
 */
public class ObjectUtils {
    public static Object deepClone(Object object) throws CloneNotSupportedException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean equals(Object object, Object other) {
        if (object != null && other != null)
            return object.equals(other);
        if (object == null && other == null)
            return true;
        return false;
    }
}
