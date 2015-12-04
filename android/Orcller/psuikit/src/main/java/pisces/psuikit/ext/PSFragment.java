package pisces.psuikit.ext;

import android.support.v4.app.Fragment;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.DataLoadValidator;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/13/15.
 */
public class PSFragment extends Fragment {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();

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
