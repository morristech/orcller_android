package pisces.psuikit.ext;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.DataLoadValidator;

/**
 * Created by pisces on 11/15/15.
 */
public class PSActivity extends Activity {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();

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
        return dataLoadValidator.isFirstLoading();
    }

    public void endDataLoading() {
        dataLoadValidator.endDataLoading();
    }

    public boolean invalidDataLoading() {
       return dataLoadValidator.invalidDataLoading();
    }
}
