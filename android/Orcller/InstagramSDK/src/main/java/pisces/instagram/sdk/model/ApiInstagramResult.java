package pisces.instagram.sdk.model;

import org.json.JSONObject;

import pisces.psfoundation.model.AbstractModel;

/**
 * Created by pisces on 11/15/15.
 */
public class ApiInstagramResult extends AbstractModel {
    public Meta meta;

    public class Meta extends AbstractModel {
        public int code;
    }
}
