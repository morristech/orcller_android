package pisces.psuikit.ext;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.DataLoadValidator;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/15/15.
 */
public class PSFragmentActivity extends FragmentActivity implements DataLoadValidator.Client {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();

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
        return dataLoadValidator.isFirstLoading();
    }

    public void endDataLoading() {
        dataLoadValidator.endDataLoading();
    }

    public boolean invalidDataLoading() {
        return dataLoadValidator.invalidDataLoading();
    }
}
