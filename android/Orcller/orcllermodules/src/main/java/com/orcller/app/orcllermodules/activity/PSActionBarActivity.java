package com.orcller.app.orcllermodules.activity;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Created by pisces on 11/12/15.
 */
public class PSActionBarActivity extends ActionBarActivity {
    private Toolbar toolbar;

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
