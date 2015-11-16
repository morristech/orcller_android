package pisces.instagram.sdk.model;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/15/15.
 */
public class ApiInstagramResult extends Model {
    public Meta meta;

    public class Meta extends Model {
        public int code;
    }
}
