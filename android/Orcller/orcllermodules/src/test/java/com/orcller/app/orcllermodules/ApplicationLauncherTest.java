package com.orcller.app.orcllermodules;

import android.app.Application;

import com.orcller.app.orcllermodules.managers.ApplicationLauncher;

import junit.framework.TestCase;

import java.lang.Exception;

/**
 * Created by pisces on 11/3/15.
 */
public class ApplicationLauncherTest extends TestCase {
    @Override
    protected void setUp() throws Exception {

    }

//    public void testGetInstance() {
//        assertNotNull(ApplicationLauncher.getInstance());
//    }

//    public void testLaunch() {
//        final CountDownLatch signal = new CountDownLatch(1);
//
//        try {
//            ApplicationLauncher.getInstance().setResource(new ApplicationLauncher.ApplicationResource
//                    ("orcller", "com.orcller.app.orcller"));
//            ApplicationLauncher.getInstance().launch();
//            assertTrue(true);
//            signal.countDown();
//        } catch (Exception e) {
//            assertTrue(false);
//            signal.countDown();
//        }
//
//        signal.wait();
//    }
}
