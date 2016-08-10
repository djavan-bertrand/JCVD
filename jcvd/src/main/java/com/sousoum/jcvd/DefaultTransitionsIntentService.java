package com.sousoum.jcvd;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.awareness.fence.FenceState;

/**
 * Created by Djavan on 13/12/2014.
 */
public class DefaultTransitionsIntentService extends IntentService {

    /**
     * Default transition intent that only post a notification when a fence from the Awareness API
     * is fired<br/>
     *
     * This class is not meant to be called, it is only a fallback if developers forgot to mention
     * their own IntentService
     */

    public DefaultTransitionsIntentService() {
        super("DefaultTransitionsIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        String notificationText;

        FenceState fenceState = FenceState.extract(intent);
        String fenceKey = fenceState.getFenceKey();
        if (fenceState.getCurrentState() == FenceState.TRUE) {

            StorableFenceManager manager = new StorableFenceManager(this);
            StorableFence fence = manager.getFence(fenceKey);
            if (fence != null) {
                notificationText = "(Default)Fence " + fenceKey + " received";
            } else {
                notificationText = "(Default)Fence " + fenceKey + " not found in store";
            }

            sendNotification(notificationText);
        }
    }

    private void sendNotification(String text) {
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.default_notif)
                .setContentTitle("Title")
                .setContentText(text);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
