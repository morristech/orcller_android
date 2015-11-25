package pisces.psfoundation.event;

/**
 * Created by pisces on 11/25/15.
 */
public class Event<T> {
    private String type;
    private T target;

    public Event(String type, T target) {
        this.type = type;
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public T getTarget() {
        return target;
    }
}
