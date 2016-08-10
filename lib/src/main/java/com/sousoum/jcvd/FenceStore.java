package com.sousoum.jcvd;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class FenceStore {

    /**
     * Package local class that stores fence
     * This implementation stores the fences in the preferences
     */

    private static final String TAG = "FenceStore";

    private static final String SHARED_PREFS = "JCVDLibStore";

    private static final String FENCE_ID_SET_KEY = "FENCE_ID_SET_KEY";

    private final String mPrefix;
    private final SharedPreferences mPrefs;

    public FenceStore(@NonNull Context context, @NonNull String prefix) {
        mPrefix = prefix;

        mPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }

    /**
     * Store a fence in the store
     * @param fence the fence to store
     */
    void storeFence(@NonNull StorableFence fence) {
        Set<String> setTmp = mPrefs.getStringSet(mPrefix + FENCE_ID_SET_KEY, null);
        HashSet<String> fenceIdSet;
        if (setTmp == null) {
            fenceIdSet = new HashSet<>();
        } else {
            fenceIdSet = new HashSet<>(setTmp);
        }

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(mPrefix + fence.getId(), StorableFence.fenceToString(fence));

        fenceIdSet.add(fence.getId());
        editor.putStringSet(mPrefix + FENCE_ID_SET_KEY, fenceIdSet);
        editor.apply();
    }

    /**
     * Remove a fence from the store based on its id
     * @param id the id of the fence to remove
     */
    public void removeFence(@NonNull String id) {
        Set<String> fenceIdSet = mPrefs.getStringSet(mPrefix + FENCE_ID_SET_KEY, null);
        if ((fenceIdSet != null) && fenceIdSet.contains(id)) {
            SharedPreferences.Editor editor = mPrefs.edit();

            editor.remove(mPrefix + id);

            fenceIdSet.remove(id);
            editor.putStringSet(FENCE_ID_SET_KEY, fenceIdSet);
            editor.apply();
        }
    }

    /**
     * Get a stored fence by its id
     * @param id the id of the fence to retrieve
     * @return a fence if found, otherwise null
     */
    private StorableFence getStoredFence(String id) {
        StorableFence fence = null;
        String jsonStr = mPrefs.getString(mPrefix + id, null);
        if (jsonStr != null) {
            fence = StorableFence.stringToFence(jsonStr);
        }

        return fence;
    }

    /**
     * Stores immediately the given fence id to the store
     * @param fenceId the id of the fence to add
     */
    public void storeFenceId(@NonNull String fenceId) {
        Set<String> setTmp = mPrefs.getStringSet(mPrefix + FENCE_ID_SET_KEY, null);
        HashSet<String> fenceIdSet;
        if (setTmp == null) {
            fenceIdSet = new HashSet<>();
        } else {
            fenceIdSet = new HashSet<>(setTmp);
        }

        SharedPreferences.Editor editor = mPrefs.edit();

        fenceIdSet.add(fenceId);
        editor.putStringSet(mPrefix + FENCE_ID_SET_KEY, fenceIdSet);

        editor.apply();
    }

    /**
     * Get all stored fences.
     * @return a list of StorableFence (can not be null)
     */
    @NonNull
    public ArrayList<StorableFence> getAllFences() {
        ArrayList<StorableFence> fenceList = new ArrayList<>();

        Set<String> setTmp = mPrefs.getStringSet(mPrefix + FENCE_ID_SET_KEY, null);
        HashSet<String> fenceIdSet;
        if (setTmp == null) {
            fenceIdSet = new HashSet<>();
        } else {
            fenceIdSet = new HashSet<>(setTmp);
        }

        for (String fenceId : fenceIdSet) {
            StorableFence storableFence = getStoredFence(fenceId);

            if (storableFence != null) {
                fenceList.add(storableFence);
            }
        }

        return fenceList;
    }

    /**
     * Get all stored fence ids.
     * @return a set of String (can not be null)
     */
    @NonNull
    public Set<String> getAllFenceIds() {
        Set<String> setTmp = mPrefs.getStringSet(mPrefix + FENCE_ID_SET_KEY, null);
        HashSet<String> fenceIdSet;
        if (setTmp == null) {
            fenceIdSet = new HashSet<>();
        } else {
            fenceIdSet = new HashSet<>(setTmp);
        }

        return fenceIdSet;
    }
}
