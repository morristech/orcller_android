package com.orcller.app.orcllermodules.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orcller.app.orcllermodules.utils.GSonUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by pisces on 11/6/15.
 */
public class AbstractModel {
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
