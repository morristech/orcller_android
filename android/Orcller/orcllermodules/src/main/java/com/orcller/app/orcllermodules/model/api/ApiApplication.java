package com.orcller.app.orcllermodules.model;

import pisces.psfoundation.model.AbstractModel;

/**
 * Created by pisces on 11/5/15.
 */
public class ApiApplication {
    public class Version extends APIResult {
        public Version.Entity entity;

        public class Entity extends AbstractModel {
            public boolean is_release;
            public String version;
            public String version_description;
        }
    }
}
