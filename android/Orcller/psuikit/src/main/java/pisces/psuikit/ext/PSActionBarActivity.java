package pisces.psuikit.ext;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import pisces.psfoundation.utils.DataLoadValidator;
import pisces.psuikit.manager.ActivityManager;
import pisces.psuikit.manager.ExceptionViewManager;
import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 11/12/15.
 */
public class PSActionBarActivity extends AppCompatActivity implements DataLoadValidator.Client, ExceptionView.Delegate {
    private boolean active;
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    protected ExceptionViewManager exceptionViewManager;
    private Toolbar toolbar;

    // ================================================================================================
    //  Overridden: AppCompatActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager.putRunningActivity(this);
        setActive(true);

        exceptionViewManager = new ExceptionViewManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ActivityManager.putRunningActivity(this);
        setActive(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        setActive(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ActivityManager.removeRunningActivity(this);
        exceptionViewManager.clear();
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFirstLoading() {
        return dataLoadValidator.isFirstLoading();
    }

    public void endDataLoading() {
        dataLoadValidator.endDataLoading();
        exceptionViewManager.validate();
    }

    public boolean invalidDataLoading() {
        return dataLoadValidator.invalidDataLoading();
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
