package pisces.psfoundation.event;

/**
 * Created by pisces on 11/25/15.
 */
public class Event<T> {
    private String type;
    private Object target;
    private Object object;

    public static <T> T cast(Object event) {
        return (T) event;
    }

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
