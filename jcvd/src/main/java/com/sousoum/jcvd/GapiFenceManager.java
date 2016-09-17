package com.sousoum.jcvd;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Class that manages addition and deletion of Fences in the Google API Client.
 * It uses a store to remember all fences that are currently in the Google API Client.
 * The store is currently backed by the shared preferences
 */
class GapiFenceManager implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Google Api Client Connection listener
     */
    interface ConnectionListener {
        /**
         * Called when the Google Api Client connects.
         */
        void onConnected();
    }

    private static final String TAG = "GapiFenceManager";

    @NonNull
    private final Context mContext;

    @NonNull
    private final GoogleApiClient mGoogleApiClient;

    @Nullable
    private ConnectionListener mConnectionListener;

    /**
     * Constructor.
     *
     * @param context a context
     */
    GapiFenceManager(@NonNull Context context) {
        mContext = context;

        mGoogleApiClient = createGapi();
    }

    @VisibleForTesting
    protected GoogleApiClient createGapi() {
        return new GoogleApiClient.Builder(mContext)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    void setConnectionListener(@Nullable ConnectionListener connectionListener) {
        mConnectionListener = connectionListener;
    }

    /**
     * Ask for a connection to the Google API Client
     */
    void connect() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    boolean isConnected() {
        return mGoogleApiClient.isConnected();
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
                            @NonNull String pendingIntentClassName, ResultCallback<Status> status) {
        if (mGoogleApiClient.isConnected()) {
            FenceUpdateRequest.Builder requestBuilder = new FenceUpdateRequest.Builder()
                    .addFence(id, fence, createRequestPendingIntent(pendingIntentClassName));

            Awareness.FenceApi.updateFences(mGoogleApiClient, requestBuilder.build())
                    .setResultCallback(status);

            return true;
        } else {
            connect();
            return false;
        }
    }

    /**
     * Ask to remove a fence from the Google API.
     * @param fenceId The id of the fence to remove.
     * @param status the status that will be called when the addition fails or succeed.
     * @return true if remove has been asked, false otherwise.
     */
    boolean removeFence(@NonNull String fenceId, ResultCallback<Status> status) {

        if (mGoogleApiClient.isConnected()) {

            FenceUpdateRequest.Builder requestBuilder = new FenceUpdateRequest.Builder()
                    .removeFence(fenceId);

            Awareness.FenceApi.updateFences(mGoogleApiClient, requestBuilder.build())
                    .setResultCallback(status);
            Log.i(TAG, "Removed " + fenceId);
            return true;
        } else {
            connect();
            return false;
        }
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

    //region GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");

        if (mConnectionListener != null) {
            mConnectionListener.onConnected();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Google API client onConnectionSuspended");
    }
    //endregion GoogleApiClient.ConnectionCallbacks

    //region GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection to Google API client failed with error code :" + connectionResult.getErrorCode());
    }
    //endregion GoogleApiClient.OnConnectionFailedListener
}
