package pisces.android.instagramsdk;

import android.test.ActivityInstrumentationTestCase2;

import pisces.instagram.sdk.activity.InstagramLoginActivity;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramLoginActivityTest extends ActivityInstrumentationTestCase2<InstagramLoginActivity> {

    private InstagramLoginActivity activity;

    public InstagramLoginActivityTest() {
        super(InstagramLoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();
    }
}
