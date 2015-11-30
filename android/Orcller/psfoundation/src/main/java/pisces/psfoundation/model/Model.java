package pisces.psfoundation.model;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.GsonUtil;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/6/15.
 */
@SuppressWarnings("serial")
public class Model implements Cloneable, Serializable {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static boolean equalsModel(Object model, Object other) {
        if (model != null && other != null)
            return ((Model) model).equalsModel((Model) other);
        if (model == null && other == null)
            return true;
        return false;
    }

    public void didChangeProperties() {
        EventBus.getDefault().post(new ModelDidChange(this));
    }

    public boolean equalsModel(Model other) {
        try {
            Field[] fields = other.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                if (Model.class.isInstance(field.get(this))) {
                    if (!Model.equalsModel(field.get(this), field.get(other)))
                        return false;
                } else if (!equals(field.get(this), field.get(other))) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public Map<String, String> map() {
        return GsonUtil.toMap(this);
    }

    public void synchronize(Model other) {
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
        return GsonUtil.toGsonString(this);
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
    //  Class: ModelDidChange
    // ================================================================================================

    public class ModelDidChange {
        private Model model;

        public ModelDidChange(Model model) {
            this.model = model;
        }

        public Model getObject() {
            return model;
        }
    }

    // ================================================================================================
    //  Class: ModelDidSynchronize
    // ================================================================================================

    public class ModelDidSynchronize {
        private Model model;

        public ModelDidSynchronize(Model model) {
            this.model = model;
        }

        public Model getObject() {
            return model;
        }
    }

    public static interface ModelUsable {
        Model getModel();
        void setModel(Model model);
    }
}
