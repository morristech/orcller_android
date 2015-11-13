package com.orcller.app.orcllermodules;

import org.junit.Test;

import static org.junit.Assert.*;

import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.model.ApplicationResource;

import java.util.concurrent.CountDownLatch;

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
            ApplicationLauncher.getDefault().setResource(new ApplicationResource
                    ("orcller"))
                    .launch();
            assertTrue(true);
            signal.countDown();
        } catch (Exception e) {
            assertTrue(false);
            signal.countDown();
        }

        try {
            signal.wait();
        } catch (Exception e) {

        }
    }
}
