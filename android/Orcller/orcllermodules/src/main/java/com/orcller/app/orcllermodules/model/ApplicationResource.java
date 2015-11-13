package com.orcller.app.orcllermodules.model;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 11/4/15.
 */
public class ApplicationResource {
    private static final String BASE_URL = "https://market.android.com/details?id=";

    private String identifier;

    public ApplicationResource(String identifier) {
        this.setIdentifier(identifier);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getInstallUrl() {
        if (Application.applicationContext().getPackageName() == null)
            return null;
        return BASE_URL + Application.applicationContext().getPackageName();
    }
}
