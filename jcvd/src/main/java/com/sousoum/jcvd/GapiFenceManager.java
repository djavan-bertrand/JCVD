package com.sousoum.jcvd;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.FenceClient;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Class that manages addition and deletion of Fences in the Google API Client.
 * It uses a store to remember all fences that are currently in the Google API Client.
 * The store is currently backed by the shared preferences
 */
class GapiFenceManager {

    private static final String TAG = "GapiFenceManager";

    @NonNull
    private final Context mContext;

    @NonNull
    private final FenceClient mFenceClient;

    /**
     * Constructor.
     *
     * @param context a context
     */
    GapiFenceManager(@NonNull Context context) {
        mContext = context;

        mFenceClient = createFenceClient();
    }

    @VisibleForTesting
    protected FenceClient createFenceClient() {
        return Awareness.getFenceClient(mContext);
    }

    /**
     * Add a fence to the Google API
     * If not connected, this will only trigger a connection.
     * This call requires that the following granted permissions:
     *      - ACCESS_FINE_LOCATION if one of the fence is a {@link StorableLocationFence}
     *      - ACTIVITY_RECOGNITION if one of the fence is a {@link StorableActivityFence}
     * @param id the unique id of the fence.
     * @param fence the fence to store
     * @param pendingIntentClassName the class name of the pending intent to call when the fence will be valid.
     * @param status the status that will be called when the addition fails or succeed.
     * @return true if add has been asked, false otherwise.
     */
    boolean addFence(@NonNull String id, @NonNull AwarenessFence fence,
                            @NonNull String pendingIntentClassName, final ResultCallback<Status> status) {

        FenceUpdateRequest.Builder requestBuilder = new FenceUpdateRequest.Builder()
                .addFence(id, fence, createRequestPendingIntent(pendingIntentClassName));

        mFenceClient.updateFences(requestBuilder.build())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            status.onResult(Status.RESULT_SUCCESS);
                        } else {
                            status.onResult(Status.RESULT_INTERNAL_ERROR);
                        }
                    }
                });
        return true;
    }

    /**
     * Ask to remove a fence from the Google API.
     * @param fenceId The id of the fence to remove.
     * @param status the status that will be called when the addition fails or succeed.
     * @return true if remove has been asked, false otherwise.
     */
    boolean removeFence(@NonNull String fenceId, final ResultCallback<Status> status) {
        FenceUpdateRequest.Builder requestBuilder = new FenceUpdateRequest.Builder()
                .removeFence(fenceId);

        mFenceClient.updateFences(requestBuilder.build()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    status.onResult(Status.RESULT_SUCCESS);
                } else {
                    status.onResult(Status.RESULT_INTERNAL_ERROR);
                }
            }
        });
        return true;
    }

    /**
     * Create a pending intent from the storable fence
     * @param pendingIntentClassName The storable fence which should contain the class name of the pending intent
     * @return The pending intent of the class if it has been successfully loaded, or a DefaultTransitionsIntentService
     */
    private PendingIntent createRequestPendingIntent(@NonNull String pendingIntentClassName) {
        PendingIntent pendingIntent = null;
        try {
            Class classOfPendingIntent = Class.forName(pendingIntentClassName);
            if (classOfPendingIntent != null) {
                // Create an Intent pointing to the IntentService
                Intent intent = new Intent(mContext, classOfPendingIntent);

                // Return a PendingIntent to start the IntentService.
                // Always create a PendingIntent sent to Location Services
                // with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
                // again updates the original. Otherwise, Location Services
                // can't match the PendingIntent to requests made with it.
                pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return pendingIntent;
    }
}
