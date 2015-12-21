package pisces.psuikit.ext;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import pisces.psfoundation.utils.DataLoadValidator;
import pisces.psuikit.manager.ExceptionViewManager;
import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 11/13/15.
 */
abstract public class PSFragment extends Fragment implements DataLoadValidator.Client, ExceptionView.Delegate {
    private boolean initialized;
    private boolean isFirstLoading = true;
    private boolean shouldStartFragment;
    private boolean viewCreated;
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    protected ExceptionViewManager exceptionViewManager = new ExceptionViewManager(this);

    // ================================================================================================
    //  Overridden: Fragment
    // ================================================================================================

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialized = true;
        viewCreated = true;

        setHasOptionsMenu(true);
        setUpViews(view);
        validateFragment();
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    public void onClick(ExceptionView view) {
    }

    public boolean shouldShowExceptionView(ExceptionView view) {
        return false;
    }

    // ================================================================================================
    //  Public
    // ============================================================================================

    public String getToolbarTitle() {
        return null;
    }

    public void invalidateFragment() {
        shouldStartFragment = true;

        if (initialized)
            validateFragment();
    }

    public boolean isUseSoftKeyboard() {
        return false;
    }

    public boolean isFirstLoading() {
        return dataLoadValidator.isFirstLoading();
    }

    public void endDataLoading() {
        dataLoadValidator.endDataLoading();
    }

    public boolean invalidDataLoading() {
        return dataLoadValidator.invalidDataLoading();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    abstract protected void setUpViews(View view);

    protected boolean isViewCreated() {
        return viewCreated;
    }

    protected void resumeFragment() {
    }

    protected void startFragment() {
    }

    private void validateFragment() {
        if (!shouldStartFragment)
            return;

        if (isFirstLoading) {
            startFragment();
            isFirstLoading = false;
        } else {
            resumeFragment();
        }

        shouldStartFragment = false;
    }
}
