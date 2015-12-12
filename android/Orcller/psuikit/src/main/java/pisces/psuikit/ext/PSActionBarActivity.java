package pisces.psuikit.ext;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.DataLoadValidator;

/**
 * Created by pisces on 11/12/15.
 */
public class PSActionBarActivity extends ActionBarActivity implements DataLoadValidator.Client {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();
    private Toolbar toolbar;

    // ================================================================================================
    //  Overridden: ActionBarActivity
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
    protected void onDestroy() {
        super.onDestroy();

        toolbar = null;
    }

    // ================================================================================================
    //  Public
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
