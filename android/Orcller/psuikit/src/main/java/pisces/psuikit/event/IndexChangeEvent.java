package pisces.psuikit.event;

import pisces.psfoundation.event.Event;

/**
 * Created by pisces on 12/4/15.
 */
public class IndexChangeEvent extends Event {
    public static final String INDEX_CHANGE = "indexChange";
    private int selectedIndex;

    public IndexChangeEvent(String type, Object target, Object object, int selectedIndex) {
        super(type, target, object);

        this.selectedIndex = selectedIndex;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}
