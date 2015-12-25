package com.orcller.app.orcller.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.activity.SplashActivity;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.PushNotificationObject;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GsonUtil;

/**
 * Created by pisces on 11/10/15.
 */
public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    public static final String PUSH_NOTIFICATION_OBJECT_KEY = "pushNotificationObject";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        final PushNotificationObject object = GsonUtil.fromJson(data.getString("object"), PushNotificationObject.class);

        if (Application.isInBackground()) {
            sendNotification(object);
        } else {
            SharedObject.get().loadNewsCountDireclty();

            Application.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Application.getTopActivity(), object.message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void sendNotification(PushNotificationObject object) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(PUSH_NOTIFICATION_OBJECT_KEY, object);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle(object.title)
                .setContentText(object.message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
