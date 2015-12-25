package pisces.psuikit.manager;

import java.util.ArrayList;
import java.util.List;

import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 12/21/15.
 */
public class ExceptionViewManager {
    private List<ExceptionView> views = new ArrayList<>();
    private ExceptionView.Delegate delegate;

    public ExceptionViewManager(ExceptionView.Delegate delegate) {
        setDelegate(delegate);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public ExceptionView.Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(ExceptionView.Delegate delegate) {
        this.delegate = delegate;

        for (ExceptionView view : views) {
            view.setDelegate(delegate);
        }
    }

    public int getViewCount() {
        return views.size();
    }

    public int getViewIndex(ExceptionView view) {
        return views.indexOf(view);
    }

    public void add(ExceptionView view) {
        view.setDelegate(delegate);
        views.add(view);
    }

    public void add(int location, ExceptionView view) {
        view.setDelegate(delegate);
        views.add(location, view);
    }

    public void add(ExceptionView... views) {
        for (ExceptionView view : views) {
            add(view);
        }
    }

    public void clear() {
        for (ExceptionView view : views) {
            view.removeFromParent();
        }
    }

    public void remove(ExceptionView view) {
        view.removeFromParent();
        views.remove(view);
    }

    public void remove(int location) {
        views.get(location).removeFromParent();
        views.remove(location);
    }

    public void removeAll() {
        for (ExceptionView view : views) {
            view.removeFromParent();
        }

        views.clear();
    }

    public boolean validate() {
        clear();

        for (ExceptionView view : views) {
            if (view.validateException())
                return true;
        }

        return false;
    }
}
