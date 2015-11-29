package pisces.psuikit.ext;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 11/15/15.
 */
public class PSActivity extends Activity {
    private boolean dataLoading;
    private boolean firstLoading = true;

    // ================================================================================================
    //  Overridden: Activity
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

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isFirstLoading() {
        return firstLoading;
    }

    public void endDataLoading() {
        dataLoading = false;
    }
    public boolean invalidDataLoading() {
        if (dataLoading)
            return true;

        dataLoading = true;
        firstLoading = false;

        return false;
    }
}
