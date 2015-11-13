package pisces.psfoundation.model;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.GSonUtil;

/**
 * Created by pisces on 11/6/15.
 */
@SuppressWarnings("serial")
public class AbstractModel implements Serializable {
    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean equals(AbstractModel other) {
        try {
            Field[] fields = other.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                field.set(this, field.get(other));

                if (!equals(field.get(this), field.get(other)))
                    return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public Map<String, String> map() {
        return GSonUtil.toMap(this);
    }

    public void synchronize(AbstractModel other) {
        if (!this.getClass().equals(other.getClass()))
            return;

        try {
            Field[] fields = other.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                field.set(this, field.get(other));

            }

            EventBus.getDefault().post(new ModelDidSynchronize(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toJson() {
        return GSonUtil.toGSonString(this);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private boolean equals(Object value, Object otherValue) {
        if (value == null && otherValue == null)
            return true;
        if (value == null || otherValue == null)
            return false;
        return value.equals(otherValue);
    }

    // ================================================================================================
    //  Class: ModelDidSynchronize
    // ================================================================================================

    public class ModelDidSynchronize {
        private AbstractModel model;

        public ModelDidSynchronize(AbstractModel model) {
            this.model = model;
        }

        public AbstractModel getObject() {
            return model;
        }
    }
}
