package pisces.android.instagramsdk;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import pisces.instagram.sdk.error.InstagramSDKError;
import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramApplicationCenterTest {
    @Test
    public void testLogin() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        InstagramApplicationCenter.getDefault().login(new InstagramApplicationCenter.CompleteHandler() {
            @Override
            public void onComplete(Model model) {
                assertTrue(true);
                signal.countDown();
            }

            @Override
            public void onError(InstagramSDKError error) {
                assertTrue(false);
                signal.countDown();
            }
        });

        try {
            signal.wait();
        } catch (InterruptedException e) {
        }
    }
}
