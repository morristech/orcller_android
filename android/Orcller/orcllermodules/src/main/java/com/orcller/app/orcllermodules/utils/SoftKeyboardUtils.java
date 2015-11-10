package com.orcller.app.orcllermodules.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.orcller.app.orcllermodules.ext.Application;

/**
 * Created by pisces on 11/11/15.
 */
public class SoftKeyboardUtils {
    public static void hide(View focusView) {
        if (!SoftKeyboardNotifier.getDefault().isHide()) {
            InputMethodManager inputMethodManager = (InputMethodManager) Application.applicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromInputMethod(focusView.getWindowToken(), 0);
            inputMethodManager.toggleSoftInput(0, 0);
        }
    }
}
