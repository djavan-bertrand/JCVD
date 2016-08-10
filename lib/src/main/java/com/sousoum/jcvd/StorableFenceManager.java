package com.sousoum.jcvd;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceQueryResult;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * Class that manages addition and deletion of Fences in the Google API Client.
 * It uses a store to remember all fences that are currently in the Google API Client.
 * The store is currently backed by the shared preferences
 */
public class StorableFenceManager implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Informs about fence addition or removal status
     */
    public interface Listener {
        /**
         * Called when a fence has been, successfully or not, added to the Google API Client
         * @param fence the fence that has been added
         * @param status the status of the operation
         */
        void fenceAddStatus(StorableFence fence, Status status);

        /**
         * Called when a fence has been, successfully or not, removed from the Google API Client
         * @param fenceId the id of the fence that has been removed
         * @param status the status of the operation
         */
        void fenceRemoveStatus(String fenceId, Status status);
    }

    private static final String TAG = "FenceManager";

    private static final String TO_ADD_STORE = "TO_ADD_STORE";
    private static final String TO_REMOVE_STORE = "TO_REMOVE_STORE";
    private static final String SYNCED_STORE = "SYNCED_STORE";

    private final Context mContext;
    private GoogleApiClient mGoogleApiClient;

    private Listener mListener;

    private final FenceStore mToAddStore; // store of the fence to add to the Google API Client
    private final FenceStore mToRemoveStore; // store of the fence to remove from the Google API Client
    private final FenceStore mSyncedStore; // store that represent which fences are in the Google API Client

    /**
     * Constructor.
     *
     * @param context a context
     */
    public StorableFenceManager(Context context) {
        mContext = context;

        mToAddStore = new FenceStore(context, TO_ADD_STORE);
        mToRemoveStore = new FenceStore(context, TO_REMOVE_STORE);
        mSyncedStore = new FenceStore(context, SYNCED_STORE);

        mGoogleApiClient = createGapi(context);

        // TODO remove this but be sure that getState is connected
        googleApiConnect();
    }

    @VisibleForTesting
    protected GoogleApiClient createGapi(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Ask for a connection to the Google API Client
     */
    private void googleApiConnect() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Set the listener. This listener will be informed when the fences are modified in the google api client
     * @param listener a listener
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Get the current listener
     * @return the current listener
     */
    public Listener getListener() {
        return mListener;
    }

    /**
     * Add a fence to the store
     * This will also add the fence to the google api client if connected. If not, it will trigger a connection
     * This call requires that the permission ACCESS_FINE_LOCATION is granted
     * @param storableFence the fence to store
     * @return true if add has been asked, false otherwise. false could be returned if the fence is expired
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public boolean addFence(@NonNull String id, @NonNull StorableFence storableFence, String pendingIntentClassName) {
        //noinspection MissingPermission
        return addFence(id, storableFence, null, pendingIntentClassName);
    }

    /**
     * Add a fence to the store
     * This will also add the fence to the google api client if connected. If not, it will trigger a connection
     * This call requires that the permission ACCESS_FINE_LOCATION is granted
     * @param storableFence the fence to store
     * @return true if add has been asked, false otherwise. false could be returned if the fence is expired
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public boolean addFence(@NonNull String id, @NonNull StorableFence storableFence,
                            @Nullable HashMap<String, Object> additionalData, String pendingIntentClassName) {
        boolean addedOngoing = false;
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            storableFence.setId(id);
            storableFence.setAdditionalData(additionalData);
            storableFence.setPendingIntentClass(pendingIntentClassName);
            mToAddStore.storeFence(storableFence);

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                FenceAddStatus addStatus = new FenceAddStatus(storableFence);

                FenceUpdateRequest.Builder requestBuilder = new FenceUpdateRequest.Builder()
                        .addFence(id, storableFence.getAwarenessFence(),
                                createRequestPendingIntent(pendingIntentClassName));

                Awareness.FenceApi.updateFences(mGoogleApiClient, requestBuilder.build())
                        .setResultCallback(addStatus);

                Log.i(TAG, "Added " + storableFence);
            } else {
                googleApiConnect();
            }

            addedOngoing = true;
        } else {
            Log.e(TAG, "Could not add the fence: permission ACCESS_FINE_LOCATION required");
        }

        return addedOngoing;
    }

    /**
     * Ask to remove a fence from the store.
     * If the Google API Client is not connected, trigger a connection
     * Else, remove from the Google API client. It will be removed from store if the operation is successful
     * @param fenceId The id of the fence to remove
     */
    public void removeFence(@NonNull String fenceId) {

        mToRemoveStore.storeFenceId(fenceId);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            FenceUpdateRequest.Builder requestBuilder = new FenceUpdateRequest.Builder()
                    .removeFence(fenceId);

            FenceRemoveStatus removeStatus = new FenceRemoveStatus(fenceId);

            Awareness.FenceApi.updateFences(mGoogleApiClient, requestBuilder.build())
                    .setResultCallback(removeStatus);
            Log.i(TAG, "Removed " + fenceId);
        } else {
            googleApiConnect();
        }
    }

    /**
     * Ask to synchronize all stored fences to the Google API Client
     */
    public void synchronizeAllFencesToGoogleApi() {
        Log.i(TAG, "Try to update list of fences");
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

                // first, add all (already) stored fences, without listener
                ArrayList<StorableFence> storedFences = mSyncedStore.getAllFences();
                if (!storedFences.isEmpty()) {
                    // for each fence, add it to the Google API Client
                    for (StorableFence storableFence : storedFences) {
                        FenceUpdateRequest.Builder requestBuilder = new FenceUpdateRequest.Builder()
                                .addFence(storableFence.getId(), storableFence.getAwarenessFence(),
                                        createRequestPendingIntent(storableFence.getPendingIntentClass()));

                        Awareness.FenceApi.updateFences(mGoogleApiClient, requestBuilder.build());
                        Log.i(TAG, "Added " + storableFence);
                    }
                    Log.i(TAG, "All already stored fences have been submitted to be synchronized with Google API Client");
                }

                // add all fences from the to add list
                ArrayList<StorableFence> toAddFences = mToAddStore.getAllFences();
                if (!toAddFences.isEmpty()) {
                    // for each fence, add it to the Google API Client
                    for (StorableFence storableFence : toAddFences) {
                        FenceAddStatus addStatus = new FenceAddStatus(storableFence);

                        FenceUpdateRequest.Builder requestBuilder = new FenceUpdateRequest.Builder()
                                .addFence(storableFence.getId(), storableFence.getAwarenessFence(),
                                        createRequestPendingIntent(storableFence.getPendingIntentClass()));

                        Awareness.FenceApi.updateFences(mGoogleApiClient, requestBuilder.build())
                                .setResultCallback(addStatus);
                        Log.i(TAG, "Added " + storableFence);
                    }
                    Log.i(TAG, "All fences to add have been submitted to be synchronized with Google API Client");
                }

                // remove all fences from the to remove list
                Set<String> toRemoveFences = mToRemoveStore.getAllFenceIds();
                if (!toRemoveFences.isEmpty()) {
                    // TODO: use only one request!
                    // for each fence, remove it to the Google API Client
                    for (String fenceId : toRemoveFences) {

                        FenceUpdateRequest.Builder requestBuilder = new FenceUpdateRequest.Builder()
                                .removeFence(fenceId);

                        FenceRemoveStatus removeStatus = new FenceRemoveStatus(fenceId);

                        Awareness.FenceApi.updateFences(mGoogleApiClient, requestBuilder.build())
                                .setResultCallback(removeStatus);
                        Log.i(TAG, "Removed " + fenceId);
                    }
                    Log.i(TAG, "All fences to remove have been submitted to be synchronized with Google API Client");
                }
            } else {
                googleApiConnect();
            }
        } else {
            Log.e(TAG, "Not able to synchronize fences because ACCESS_FINE_LOCATION permission is required.");
        }
    }

    /**
     * Create a pending intent from the storable fence
     * @param pendingIntentClassName The storable fence which should contain the class name of the pending intent
     * @return The pending intent of the class if it has been successfully loaded, or a DefaultTransitionsIntentService
     */
    private PendingIntent createRequestPendingIntent(String pendingIntentClassName) {
        Class classOfPendingIntent = DefaultTransitionsIntentService.class;
        if (pendingIntentClassName != null) {
            try {
                Class classOfPendingIntentTmp = Class.forName(pendingIntentClassName);
                if (classOfPendingIntentTmp != null) {
                    classOfPendingIntent = classOfPendingIntentTmp;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Create an Intent pointing to the IntentService
        Intent intent = new Intent(mContext, classOfPendingIntent);

        /*
         * Return a PendingIntent to start the IntentService.
         * Always create a PendingIntent sent to Location Services
         * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
         * again updates the original. Otherwise, Location Services
         * can't match the PendingIntent to requests made with it.
         */
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Get the state of a given fence
     * @param fence the fence to query
     * @deprecated
     */
    // TODO: remove
    public void getFenceState(final StorableFence fence) {
        Awareness.FenceApi.queryFences(mGoogleApiClient,
                FenceQueryRequest.forFences(Arrays.asList(fence.getId())))
                .setResultCallback(new ResultCallback<FenceQueryResult>() {
                    @Override
                    public void onResult(@NonNull FenceQueryResult fenceQueryResult) {
                        if (!fenceQueryResult.getStatus().isSuccess()) {
                            Toast.makeText(mContext, "Could not query fence: " + fence.getId(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FenceStateMap map = fenceQueryResult.getFenceStateMap();
                        for (String fenceKey : map.getFenceKeys()) {
                            FenceState fenceState = map.getFenceState(fenceKey);
                            Toast.makeText(mContext, "Fence " + fenceKey + ": "
                                    + fenceState.getCurrentState()
                                    + ", was="
                                    + fenceState.getPreviousState()
                                    + ", lastUpdateTime="
                                    + DateFormat.getTimeInstance(DateFormat.SHORT).format(
                                    new Date(fenceState.getLastFenceUpdateTimeMillis())), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Get all stored fences that are synced with Google API Client.
     * @return a list of StorableFence (can not be null)
     */
    public @NonNull
    ArrayList<StorableFence> getAllFences() {
        return mSyncedStore.getAllFences();
    }

    /**
     * Get a stored fence which is synced with Google API Client.
     * @param id the id of the searched fence
     * @return a StorableFence that matches the given id
     */
    public StorableFence getFence(String id) {
        StorableFence storableFence = null;

        if (id != null) {
            ArrayList<StorableFence> allGeo = getAllFences();

            for (StorableFence currentFence : allGeo) {
                if (currentFence.getId() != null && currentFence.getId().equals(id)) {
                    storableFence = currentFence;
                    break;
                }
            }
        }
        return storableFence;
    }

    //region GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");

        synchronizeAllFencesToGoogleApi();

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

    //region Result callbacks
    private class FenceRemoveStatus implements ResultCallback<Status> {

        /**
         * Inner class that will responds to ResultCallback when a fence will be, successfully or not, removed from the Google API Client
         */

        private final String mFenceId;
        public FenceRemoveStatus(@NonNull String fenceId) {
            mFenceId = fenceId;
        }

        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                Log.i(TAG, "Removed successfully fence " + mFenceId + " to the Google API");
                // since the operation is successful, remove from the local store
                mSyncedStore.removeFence(mFenceId);

                mToRemoveStore.removeFence(mFenceId);
            } else {
                Log.e(TAG, "Error : fence not removed. Error is " + status.getStatusMessage() + "(code : " + status.getStatusCode() + ")");
            }

            if (mListener != null) {
                mListener.fenceRemoveStatus(mFenceId, status);
            }
        }
    }

    private class FenceAddStatus implements ResultCallback<Status> {

        /**
         * Inner class that will responds to ResultCallback when a fence will be, successfully or not, added to the Google API Client
         */

        private final StorableFence mFence;

        public FenceAddStatus(@NonNull StorableFence fence) {
            mFence = fence;
        }

        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                Log.i(TAG, "Added successfully fence " + mFence + " to the Google API");
                // since the operation is successful, remove from the local store
                mSyncedStore.storeFence(mFence);

                mToAddStore.removeFence(mFence.getId());
            } else {
                Log.e(TAG, "Error : fence not added. Error is " + status.getStatusMessage() + "(code : " + status.getStatusCode() + ")");
            }

            if (mListener != null) {
                mListener.fenceAddStatus(mFence, status);
            }
        }
    }
    //endregion Result callbacks
}
