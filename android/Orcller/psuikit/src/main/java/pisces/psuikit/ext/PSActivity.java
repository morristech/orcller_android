package pisces.psuikit.ext;

import android.app.Activity;
import android.os.Bundle;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 11/15/15.
 */
public class PSActivity extends Activity {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
