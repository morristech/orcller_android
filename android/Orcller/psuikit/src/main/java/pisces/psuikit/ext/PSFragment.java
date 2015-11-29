package pisces.psuikit.ext;

import android.support.v4.app.Fragment;

import de.greenrobot.event.EventBus;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/13/15.
 */
public class PSFragment extends Fragment {
    private boolean dataLoading;
    private boolean firstLoading = true;

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
        return firstLoading;
    }

    public void endDataLoading() {
        dataLoading = false;
        firstLoading = false;
    }
    public boolean invalidDataLoading() {
        if (dataLoading)
            return true;

        dataLoading = true;

        return false;
    }
}
