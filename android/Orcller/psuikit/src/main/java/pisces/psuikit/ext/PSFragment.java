package pisces.psuikit.ext;

import android.support.v4.app.Fragment;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.DataLoadValidator;
import pisces.psuikit.manager.ExceptionViewManager;
import pisces.psuikit.manager.ProgressBarManager;
import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 11/13/15.
 */
public class PSFragment extends Fragment implements DataLoadValidator.Client, ExceptionView.Delegate {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    protected ExceptionViewManager exceptionViewManager = new ExceptionViewManager(this);

    // ================================================================================================
    //  Overridden: Fragment
    // ================================================================================================

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
        ProgressBarManager.hide(getActivity());
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

    public boolean isFirstLoading() {
        return dataLoadValidator.isFirstLoading();
    }

    public void endDataLoading() {
        dataLoadValidator.endDataLoading();
    }

    public boolean invalidDataLoading() {
        return dataLoadValidator.invalidDataLoading();
    }
}
