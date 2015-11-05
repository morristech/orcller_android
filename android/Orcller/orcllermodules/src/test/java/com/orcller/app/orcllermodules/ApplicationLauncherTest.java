package com.orcller.app.orcllermodules;

import org.junit.Test;

import static org.junit.Assert.*;

import com.orcller.app.orcllermodules.managers.ApplicationLauncher;

/**
 * Created by pisces on 11/3/15.
 */
public class ApplicationLauncherTest {
    @Test
    public void testGeDefault() {
        assertNotNull(ApplicationLauncher.getDefault());
    }

    @Test
    public void testLaunch() {
        final CountDownLatch signal = new CountDownLatch(1);

        try {
            ApplicationLauncher.getDefault().setResource(new ApplicationLauncher.ApplicationResource
                    ("orcller"))
                    .launch();
            assertTrue(true);
            signal.countDown();
        } catch (Exception e) {
            assertTrue(false);
            signal.countDown();
        }

        signal.wait();
    }
}
