package pisces.psfoundation.ext;

import pisces.psfoundation.utils.DataLoadValidator;

/**
 * Created by pisces on 12/10/15.
 */
public class PSObject implements DataLoadValidator.Client {
    protected DataLoadValidator dataLoadValidator = new DataLoadValidator();

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
}
