package pisces.psfoundation.model;


import android.os.AsyncTask;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
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
            Field[] fields = getClass().getFields();

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
        EventBus.getDefault().post(new Event(Event.CHANGE, this));
    }

    public boolean equalsModel(Model other) {
        try {
            Field[] fields = other.getClass().getFields();

            for (Field field : fields) {
                field.setAccessible(true);

                Object object = field.get(this);
                Object otherObject = field.get(other);

                if (!equals(object, otherObject))
                    return false;
            }

            return true;
        } catch (Exception e) {
            Log.d("Exception", e);
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

    public void synchronize(Model other) {
        synchronize(other, null);
    }

    public void synchronize(final Model other, final Runnable runnable) {
        synchronize(other, runnable, false);
    }

    public void synchronize(final Model other, boolean postEnabled) {
        synchronize(other, null, postEnabled);
    }

    public void synchronize(final Model other, final Runnable runnable, final boolean postEnabled) {
        if (!this.getClass().equals(other.getClass()) || other == null)
            return;

        final Model self = this;

        Application.run(new Runnable() {
            @Override
            public void run() {
                try {
                    Field[] fields = other.getClass().getFields();

                    for (Field field : fields) {
                        field.setAccessible(true);
                        field.set(self, field.get(other));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (runnable != null)
                    runnable.run();

                if (postEnabled)
                    EventBus.getDefault().post(new Event(Event.SYNCHRONIZE, self));
            }
        });
    }

    public String toJson() {
        return GsonUtil.toGsonString(this);
    }

    public Map<String, Object> toMap() {
        return GsonUtil.toMap(this);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private boolean equals(Object object, Object otherObject) {
        if (object == null && otherObject == null)
            return true;
        if ((object != null && otherObject == null) || (object == null && otherObject != null))
            return false;
        if (!object.getClass().equals(otherObject.getClass()))
            return false;
        if (Model.class.isInstance(object) && !Model.equalsModel(object, otherObject))
            return false;
        if (List.class.isInstance(object) && !equalsList((List) object, (List) otherObject))
            return false;
        if (Map.class.isInstance(object) && !equalsMap((Map) object, (Map) otherObject))
            return false;
        if (!ObjectUtils.equals(object, otherObject))
            return false;
        return true;
    }

    private boolean equalsList(List list, List otherList) {
        if (list == null && otherList == null)
            return true;
        if (list == null || otherList == null)
            return false;

        int count = list.size();
        int otherCount = otherList.size();

        if (count != otherCount)
            return false;

        int i = 0;
        for (Object object : list) {
            Object otherObject = otherList.get(i);

            if (!equals(object, otherObject))
                return false;

            i++;
        }

        return true;
    }

    private boolean equalsMap(Map map, Map otherMap) {
        if (map == null && otherMap == null)
            return true;
        if (map == null || otherMap == null)
            return false;

        int count = map.size();
        int otherCount = otherMap.size();

        if (count != otherCount)
            return false;

        int i = 0;
        Iterator entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Object key = entry.getKey();
            Object object = map.get(key);
            Object otherObject = otherMap.get(key);

            if (!equals(object, otherObject))
                return false;

            i++;
        }

        return true;
    }

    // ================================================================================================
    //  Class: Event
    // ================================================================================================

    public static class Event extends pisces.psfoundation.event.Event {
        public static final String CHANGE = "change";
        public static final String SYNCHRONIZE = "synchronize";

        public Event(String type, Object target) {
            super(type, target);
        }

        public Model getModel() {
            return getTarget() != null ? (Model) getTarget() : null;
        }
    }

    // ================================================================================================
    //  Interface: ModelUsable
    // ================================================================================================

    public static interface ModelUsable {
        Model getModel();
        void setModel(Model model);
    }

    // ================================================================================================
    //  Interface: EqualsCompletion
    // ================================================================================================

    public static interface EqualsCompletion {
        void onComplete(boolean equals);
    }
}
