package com.sousoum.jcvd;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Class that manages addition and deletion of Fences in the Google API Client.
 * It uses a store to remember all fences that are currently in the Google API Client.
 * The store is currently backed by the shared preferences
 */
public class StorableFenceManager {

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

    private Listener mListener;

    @VisibleForTesting
    final FenceStore mToAddStore; // store of the fence to add to the Google API Client
    @VisibleForTesting
    final FenceStore mToRemoveStore; // store of the fence to remove from the Google API Client
    @VisibleForTesting
    final FenceStore mSyncedStore; // store that represent which fences are in the Google API Client

    private final GapiFenceManager mGapiFenceManager;

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

        mGapiFenceManager = createGapiFenceManager();
        mGapiFenceManager.setConnectionListener(mConnectionListener);
    }

    @VisibleForTesting
    protected GapiFenceManager createGapiFenceManager() {
        return new GapiFenceManager(mContext);
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
     * This call requires that the following granted permissions:
     *      - ACCESS_FINE_LOCATION if one of the fence is a {@link StorableLocationFence}
     *      - ACTIVITY_RECOGNITION if one of the fence is a {@link StorableActivityFence}
     * @param id the unique id of the fence. You will be able to get the fence with this id.
     * @param storableFence the fence to store
     * @param pendingIntentClassName the class name of the pending intent to call when the fence will be valid.
     */
    public void addFence(@NonNull String id, @NonNull StorableFence storableFence,
                            @NonNull String pendingIntentClassName) {
        addFence(id, storableFence, null, pendingIntentClassName);
    }

    /**
     * Add a fence to the store
     * This will also add the fence to the google api client if connected. If not, it will trigger a connection
     * This call requires that the following granted permissions:
     *      - ACCESS_FINE_LOCATION if one of the fence is a {@link StorableLocationFence}
     *      - ACTIVITY_RECOGNITION if one of the fence is a {@link StorableActivityFence}
     * @param id the unique id of the fence. You will be able to get the fence with this id.
     * @param storableFence the fence to store
     * @param additionalData a hash map associated with this fence. Can be null.
     * @param pendingIntentClassName the class name of the pending intent to call when the fence will be valid.
     */
    public void addFence(@NonNull String id, @NonNull StorableFence storableFence,
                            @Nullable HashMap<String, Object> additionalData,
                            @NonNull String pendingIntentClassName) {
        storableFence.setId(id);
        storableFence.setAdditionalData(additionalData);
        storableFence.setPendingIntentClass(pendingIntentClassName);
        mToAddStore.storeFence(storableFence);

        FenceAddStatus addStatus = new FenceAddStatus(storableFence);
        mGapiFenceManager.addFence(id, storableFence.getAwarenessFence(mContext),
                pendingIntentClassName, addStatus);
    }

    /**
     * Ask to remove a fence from the store.
     * If the Google API Client is not connected, trigger a connection
     * Else, remove from the Google API client. It will be removed from store if the operation is successful
     * @param fenceId The id of the fence to remove
     */
    public void removeFence(@NonNull String fenceId) {

        mToRemoveStore.storeFenceId(fenceId);

        FenceRemoveStatus removeStatus = new FenceRemoveStatus(fenceId);
        mGapiFenceManager.removeFence(fenceId, removeStatus);
    }

    /**
     * Ask to synchronize all stored fences to the Google API Client
     */
    private void synchronizeAllFencesToGoogleApi() {
        Log.i(TAG, "Try to update list of fences");
        if (mGapiFenceManager.isConnected()) {
            //TODO: see if this part is really needed
            /*// first, add all (already) stored fences, without listener
            ArrayList<StorableFence> storedFences = mSyncedStore.getAllFences();
            if (!storedFences.isEmpty()) {
                // for each fence, add it to the Google API Client
                for (StorableFence storableFence : storedFences) {
                    if ((storableFence.getId() != null) &&
                            (storableFence.getPendingIntentClass() != null)) {
                        mGapiFenceManager.addFence(storableFence.getId(), storableFence.getAwarenessFence(mContext),
                                storableFence.getPendingIntentClass(), null);
                    }
                    Log.i(TAG, "Added " + storableFence);
                }
                Log.i(TAG, "All already stored fences have been submitted to be synchronized with Google API Client");
            }*/

            // add all fences from the to add list
            ArrayList<StorableFence> toAddFences = mToAddStore.getAllFences();
            if (!toAddFences.isEmpty()) {
                // for each fence, add it to the Google API Client
                for (StorableFence storableFence : toAddFences) {
                    FenceAddStatus addStatus = new FenceAddStatus(storableFence);

                    if ((storableFence.getId() != null) &&
                            (storableFence.getPendingIntentClass() != null)) {
                        mGapiFenceManager.addFence(storableFence.getId(), storableFence.getAwarenessFence(mContext),
                                storableFence.getPendingIntentClass(), addStatus);
                    }
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

                    FenceRemoveStatus removeStatus = new FenceRemoveStatus(fenceId);
                    mGapiFenceManager.removeFence(fenceId, removeStatus);
                    Log.i(TAG, "Removed " + fenceId);
                }
                Log.i(TAG, "All fences to remove have been submitted to be synchronized with Google API Client");
            }
        } else {
            mGapiFenceManager.connect();
        }
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

                // id could not be null here as we have added the fence to the store
                assert mFence.getId() != null;
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

    @VisibleForTesting
    protected final GapiFenceManager.ConnectionListener mConnectionListener =
            new GapiFenceManager.ConnectionListener() {

        @Override
        public void onConnected() {
            synchronizeAllFencesToGoogleApi();
        }
    };
}
