package com.orcller.app.orcller;

import android.content.Context;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import pisces.psfoundation.ext.Application;

/**
 * A collection of Google Analytics trackers. Fetch the tracker you need using
 * {@code AnalyticsTrackers.getInstance().get(...)}
 * <p/>
 * This code was generated by Android Studio but can be safely modified by
 * hand at this point.
 * <p/>
 * TODO: Call {@link #initialize(Context)} from an entry point in your app
 * before using this!
 */
public final class AnalyticsTrackers {

    public enum Target {
        APP
    }

    private static AnalyticsTrackers sInstance;

    public static synchronized void clear() {
        sInstance = null;
    }

    public static void init() {
        getInstance().get(Target.APP);
    }

    public static synchronized AnalyticsTrackers getInstance() {
        if(sInstance == null) {
            synchronized(TransferUtility.class) {
                sInstance = new AnalyticsTrackers(Application.applicationContext());
            }
        }
        return sInstance;
    }

    private final Map<Target, Tracker> mTrackers = new HashMap<>();
    private final Map<Target, Map<String, String>> mScreenNameMaps = new HashMap<>();
    private final Context mContext;

    /**
     * Don't instantiate directly - use {@link #getInstance()} instead.
     */
    private AnalyticsTrackers(Context context) {
        mContext = context.getApplicationContext();
    }

    public synchronized Tracker get(Target target) {
        if (!mTrackers.containsKey(target)) {
            Tracker tracker;
            switch (target) {
                case APP:
                    tracker = GoogleAnalytics.getInstance(mContext).newTracker(R.xml.app_tracker);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled analytics target " + target);
            }
            mTrackers.put(target, tracker);
        }

        return mTrackers.get(target);
    }

    public void getScreenText(final Target target, final String screenName, final CompleteHandler completeHandler) {
        Application.run(new Runnable() {
            @Override
            public void run() {
                if (!mScreenNameMaps.containsKey(target)) {
                    Map<String, String> map = null;

                    switch (target) {
                        case APP:
                            try {
                                map = new LinkedHashMap<>();
                                XmlPullParser parser = mContext.getResources().getXml(R.xml.app_tracker);
                                String screenName = null;

                                while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                                    if (parser.getEventType() == XmlPullParser.START_TAG) {
                                        if (parser.getName().equals("screenName"))
                                            screenName = parser.getAttributeValue(0);
                                    } else if (parser.getEventType() == XmlPullParser.TEXT) {
                                        map.put(screenName, parser.getText());
                                    }
                                    parser.next();
                                }
                            } catch(Throwable t) {
                            }
                            break;
                        default:
                            break;
                    }

                    if (map != null)
                        mScreenNameMaps.put(target, map);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (completeHandler != null && mScreenNameMaps.get(target).containsKey(screenName))
                    completeHandler.onComplete(mScreenNameMaps.get(target).get(screenName));
            }
        });
    }

    public void trackScreen(final Target target, String screenName) {
        getScreenText(target, screenName,
                new CompleteHandler() {
                    @Override
                    public void onComplete(final String text) {
                        Tracker tracker = AnalyticsTrackers.getInstance().get(target);
                        tracker.setScreenName(text);
                        tracker.send(new HitBuilders.AppViewBuilder().build());
                    }
                });
    }

    public interface CompleteHandler {
        void onComplete(String text);
    }
}
