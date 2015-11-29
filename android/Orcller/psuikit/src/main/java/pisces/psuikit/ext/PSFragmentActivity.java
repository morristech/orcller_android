package pisces.psuikit.ext;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/15/15.
 */
public class PSFragmentActivity extends FragmentActivity {
    private boolean dataLoading;
    private boolean firstLoading = true;

    // ================================================================================================
    //  Overridden: FragmentActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application.setTopActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Application.setTopActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        ProgressBarManager.hide(this);
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
