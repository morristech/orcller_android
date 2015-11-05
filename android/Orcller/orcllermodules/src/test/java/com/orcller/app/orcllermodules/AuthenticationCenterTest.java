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
        AuthenticationCenter.getDefault().setTestUserSessionToken("8mhO9Ra6lVENUYvLj50QdWVpcvzUYk+8nt2yec4b/7knfvNYhO61ziJ5hWykaJpfG2Xfm5DxQc37Uo1oVtUi0Vfi1HmBMJ8LQ864fHr83fP0WH00Hs7ifi2LNAG5a1GFZguPQBcVgHhRisvD/Z0XGQ==");
        .synchorinze();
    }
}
