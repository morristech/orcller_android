package pisces.psfoundation.utils;

/**
 * Created by pisces on 12/5/15.
 */
public class DataLoadValidator {
    private boolean dataLoading;
    private boolean firstLoading = true;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public boolean isFirstLoading() {
        return firstLoading;
    }

    public void endDataLoading() {
        dataLoading = false;
        firstLoading = false;
    }

    public boolean invalidDataLoading() {
        if (dataLoading)
            return true;

        dataLoading = true;

        return false;
    }

    // ================================================================================================
    //  Interface: Client
    // ================================================================================================

    public static interface Client {
        void endDataLoading();
        boolean isFirstLoading();
        boolean invalidDataLoading();
    }
}
