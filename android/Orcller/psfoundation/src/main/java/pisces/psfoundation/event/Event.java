package pisces.psfoundation.event;

import java.io.Serializable;

/**
 * Created by pisces on 11/25/15.
 */
public class Event implements Serializable {
    private String type;
    private Object target;
    private Object object;

    public Event(Object target) {
        this.target = target;
    }

    public Event(String type, Object target) {
        this.type = type;
        this.target = target;
    }

    public Event(String type, Object target, Object object) {
        this.type = type;
        this.target = target;
        this.object = object;
    }

    public String getType() {
        return type;
    }

    public Object getTarget() {
        return target;
    }

    public Object getObject() {
        return object;
    }
}
