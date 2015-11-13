package com.orcller.app.orcllermodules;

import org.junit.Test;

import static org.junit.Assert.*;

import com.orcller.app.orcllermodules.managers.AuthenticationCenter;

/**
 * Created by pisces on 11/3/15.
 */
public class AuthenticationCenterTest {
    @Test
    public void testGeDefault() {
        assertNotNull(AuthenticationCenter.getDefault());
    }

    @Test
    public void testSynchronize() {
    }
}
