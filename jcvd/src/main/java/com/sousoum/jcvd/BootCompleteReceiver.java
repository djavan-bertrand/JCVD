package com.sousoum.jcvd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by djavan on 13/08/2017.
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        StorableFenceManager manager = new StorableFenceManager(context);
        manager.synchronizeAllToGoogleApi();
    }
}
