package com.orcller.app.orcllermodules.model;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/5/15.
 */
public class ApiApplication {
    public class Version extends ApiResult<Version.Entity> {
        public class Entity extends Model {
            public boolean is_release;
            public String version;
            public String version_description;
        }
    }
}
