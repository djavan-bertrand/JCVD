package com.sousoum.jcvdexample;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.awareness.fence.FenceState;
import com.sousoum.jcvd.StorableFence;
import com.sousoum.jcvd.StorableFenceManager;

/**
 * Created by Djavan on 13/12/2014.
 */
public class CustomTransitionsIntentService extends IntentService {

    public static final String TEST_CHANNEL = "TEST_CHANNEL";

    public CustomTransitionsIntentService() {
        super("CustomTransitionsIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        String notificationText;

        FenceState fenceState = FenceState.extract(intent);
        String fenceKey = fenceState.getFenceKey();
        if (fenceState.getCurrentState() == FenceState.TRUE) {
            StorableFenceManager manager = new StorableFenceManager(this);
            StorableFence fence = manager.getFence(fenceKey);
            if (fence != null) {
                notificationText = "(Custom)Fence " + fenceKey + " received";
            } else {
                notificationText = "(Custom)Fence " + fenceKey + " not found in store";
            }

            sendNotification(notificationText);
        }
    }

    private void sendNotification(String text) {
        createNotificationChannel();
        Notification notif = new NotificationCompat.Builder(this, TEST_CHANNEL)
                .setSmallIcon(R.drawable.default_notif)
                .setContentTitle("Custom")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .build();


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Issue the notification
        notificationManager.notify(0, notif);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Test";
            String description = "Test channel, will display test notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(TEST_CHANNEL, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
