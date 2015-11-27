package pisces.psuikit.event;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 11/27/15.
 */
public class ImagePickerEvent extends Event {
    public static final String COMPLETE_SELECTION = "completeSelection";

    public ImagePickerEvent(String type, Object target, Object object) {
        super(type, target, object);
    }
}
