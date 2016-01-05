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
public class PSFragment extends Fragment implements DataLoadValidator.Client, ExceptionView.Delegate {
    private boolean active;
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;
    protected DataLoadValidator dataLoadValidator;
    protected ExceptionViewManager exceptionViewManager;

    // ================================================================================================
    //  Overridden: Fragment
    // ================================================================================================

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataLoadValidator = new DataLoadValidator();
        exceptionViewManager = new ExceptionViewManager(this);

        if (!initializedSubviews) {
            initializedSubviews = true;

            setUpSubviews(view);
        }

        invalidateProperties();
    }

    @Override
    public void onResume() {
        super.onResume();

        setActive(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        setActive(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        initializedSubviews = false;
        dataLoadValidator.endDataLoading();
        exceptionViewManager.clear();
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
    // ================================================================================================

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getToolbarTitle() {
        return null;
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

    public boolean isImmediatelyUpdating() {
        return immediatelyUpdating;
    }

    public void setImmediatelyUpdating(boolean immediatelyUpdating) {
        this.immediatelyUpdating = immediatelyUpdating;
    }

    public void invalidateProperties() {
        if (initializedSubviews || immediatelyUpdating)
            commitProperties();
    }

    public void validateProperties() {
        commitProperties();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected void commitProperties() {
    }

    protected void setUpSubviews(View view) {
    }
}
