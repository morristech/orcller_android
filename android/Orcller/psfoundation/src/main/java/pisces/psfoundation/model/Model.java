package pisces.psfoundation.model;


import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.GsonUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;

/**
 * Created by pisces on 11/6/15.
 */
@SuppressWarnings("serial")
public class Model implements Cloneable, Serializable {
    // ================================================================================================
    //  Impl: Cloneable
    // ================================================================================================

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            Object clone = super.clone();
            Field[] fields = getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                Object object = field.get(this);

                if (Model.class.isInstance(object)) {
                    field.set(clone, ((Model) object).clone());
                } else if (ArrayList.class.isInstance(object)) {
                    field.set(clone, ((ArrayList) object).clone());
                } else if (HashMap.class.isInstance(object)) {
                    field.set(clone, ((HashMap) object).clone());
                }
            }

            return clone;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Model deepClone() throws CloneNotSupportedException {
        try {
            return (Model) ObjectUtils.deepClone(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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

    public boolean equalsList(Object object, Object other) {
        List list = (List) object;
        List otherList = (List) other;

        if (list == null || otherList == null)
            return false;

        if (list.size() != otherList.size())
            return false;

        for (int i=0; i<list.size(); i++) {
            Object object1 = list.get(i);
            Object object2 = otherList.get(i);

            if (!Model.equalsModel(object1, object2))
                return false;
        }

        return true;
    }

    public boolean equalsModel(Model other) {
        try {
            Field[] fields = other.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                Object object = field.get(this);

                if (Model.class.isInstance(object)) {
                    if (!Model.equalsModel(object, field.get(other)))
                        return false;
                } else if (!equals(object, field.get(other))) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void equalsModel(final Model other, final EqualsCompletion completion) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return equalsModel(other);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if (completion != null)
                    completion.onComplete(result);
            }
        }.execute();
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

    // ================================================================================================
    //  Interface: ModelDidSynchronize
    // ================================================================================================

    public static interface EqualsCompletion {
        void onComplete(boolean equals);
    }
}
