package com.orcller.app.orcllermodules.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import com.orcller.app.orcllermodules.event.SoftKeyboardEvent;

import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by pisces on 11/8/15.
 */
public final class SoftKeyboardNotifier {
    private static SoftKeyboardNotifier uniqueInstance;
    private HashMap<String, ViewTreeObserver.OnGlobalLayoutListener> handlerMap;
    private boolean isHide;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public SoftKeyboardNotifier() {
        handlerMap = new HashMap<String, ViewTreeObserver.OnGlobalLayoutListener>();
    }

    public static SoftKeyboardNotifier getDefault() {
        if(uniqueInstance == null) {
            synchronized(SoftKeyboardNotifier.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new SoftKeyboardNotifier();
                }
            }
        }
        return uniqueInstance;
    }

    public boolean isHide() {
        return isHide;
    }

    public void register(Activity activity){
        if (activity == null)
            return;

        String key = String.valueOf(activity.hashCode());

        if (!handlerMap.containsKey(key)) {
            final View decorView = activity.getWindow().getDecorView();
            ViewTreeObserver.OnGlobalLayoutListener handler = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect rect = new Rect();
                    decorView.getWindowVisibleDisplayFrame(rect);
                    int displayHight = rect.bottom - rect.top;
                    int hight = decorView.getHeight();
                    boolean hide = (double) displayHight / hight > 0.8;

                    if (hide != isHide) {
                        isHide = hide;

                        if (isHide)
                            EventBus.getDefault().post(new SoftKeyboardEvent.Hide());
                        else
                            EventBus.getDefault().post(new SoftKeyboardEvent.Show());
                    }
                }
            };

            decorView.getViewTreeObserver().addOnGlobalLayoutListener(handler);
            handlerMap.put(key, handler);
        }
    }

    public void unregister(Activity activity) {
        if (activity == null)
            return;

        String key = String.valueOf(activity.hashCode());

        if (handlerMap.containsKey(key)) {
            final View decorView = activity.getWindow().getDecorView();
            ViewTreeObserver.OnGlobalLayoutListener handler = handlerMap.get(key);
            decorView.getViewTreeObserver().removeOnGlobalLayoutListener(handler);
            handlerMap.remove(key);
        }
    }
}
