package pisces.psuikit.event;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 11/9/15.
 */
public class SoftKeyboardEvent extends Event {
    public static final String HIDE = "hide";
    public static final String SHOW = "show";

    public SoftKeyboardEvent(String type, Object object) {
        super(type, null, object);
    }
}