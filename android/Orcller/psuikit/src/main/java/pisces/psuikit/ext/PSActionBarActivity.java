package pisces.psuikit.ext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.DataLoadValidator;
import pisces.psfoundation.utils.Log;
import pisces.psuikit.manager.ActivityManager;

/**
 * Created by pisces on 11/12/15.
 */
public class PSActionBarActivity extends AppCompatActivity implements DataLoadValidator.Client {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    private Toolbar toolbar;

    // ================================================================================================
    //  Overridden: AppCompatActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager.putRunningActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ActivityManager.putRunningActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ActivityManager.removeRunningActivity(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        super.startActivity(intent);
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

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected Toolbar getToolbar() {
        return toolbar;
    }

    protected void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
