package com.sousoum.jcvd;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.awareness.fence.AwarenessFence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A storable fence that backs up a {@link AwarenessFence}.
 * It is from this class that {@link StorableActivityFence}, {@link StorableTimeFence},
 * {@link StorableHeadphoneFence} and {@link StorableLocationFence} inherit.
 * The instances of this class are meta fences: they are a and, or or not of a fence.
 */
public class StorableFence {
    /** Type of the fence. */
    public enum Type {
        /**
         * Fence is a meta fence,
         * It regroups other fences in {@link StorableFence#getAndFences()} or
         * {@link StorableFence#getOrFences()} or {@link StorableFence#getNotFence()}
         */
        META,

        /**
         * Fence is a location fence
         * It can be casted into a {@link StorableLocationFence}
         */
        LOCATION,

        /**
         * Fence is an activity fence
         * It can be casted into a {@link StorableActivityFence}
         */
        ACTIVITY,

        /**
         * Fence is a time fence
         * It can be casted into a {@link StorableTimeFence}
         */
        TIME,

        /**
         * Fence is a headphone fence
         * It can be casted into a {@link StorableHeadphoneFence}
         */
        HEADPHONE,
    }

    @NonNull
    private final ArrayList<StorableFence> mAndFences;

    @NonNull
    private final ArrayList<StorableFence> mOrFences;

    @Nullable
    private StorableFence mNotFence;

    @NonNull
    private final Type mType;

    private String mId;

    private String mPendingIntentClass;

    @NonNull
    private final HashMap<String, Object> mAdditionalData;

    protected final static String FENCE_TYPE_KEY = "type";
    private final static String FENCE_ID_KEY = "id";
    private final static String FENCE_PENDING_INTENT_CLASS_KEY = "pendingIntentClass";
    private final static String FENCE_ADDITIONAL_DATA_KEY = "additionalData";
    private final static String ADDITIONAL_DATA_TYPE_KEY = "type";
    private final static String ADDITIONAL_DATA_VALUE_KEY = "value";
    private final static String FENCE_META_AND_KEY = "and";
    private final static String FENCE_META_OR_KEY = "or";
    private final static String FENCE_META_NOT_KEY = "not";

    protected StorableFence(@NonNull Type type) {
        mType = type;
        mAndFences = new ArrayList<>();
        mOrFences = new ArrayList<>();
        mAdditionalData = new HashMap<>();
    }

    //region getters

    /**
     * Gets the id of the fence.
     * This id is a unique identifier for the fence
     * A fence has an id only if it is the root fence that has been added to the
     * StorableFenceManager with
     * {@link StorableFenceManager#addFence(String, StorableFence, String)} or
     * {@link StorableFenceManager#addFence(String, StorableFence, HashMap, String)}.
     * @return an id if it exist, null otherwise.
     */
    @Nullable
    public String getId() {
        return mId;
    }

    /**
     * Gets the and fences.
     * Only available for a {@link Type#META} fence.
     * @return a list of all and fences. Can not be null.
     */
    @NonNull
    public ArrayList<StorableFence> getAndFences() {
        return mAndFences;
    }

    /**
     * Gets the or fences.
     * Only available for a {@link Type#META} fence.
     * @return a list of all or fences. Can not be null.
     */
    @NonNull
    public ArrayList<StorableFence> getOrFences() {
        return mOrFences;
    }

    /**
     * Gets the not fence.
     * Only available for a {@link Type#META} fence.
     * @return the not fence. Null if there is no not fence.
     */
    @Nullable
    public StorableFence getNotFence() {
        return mNotFence;
    }

    /**
     * Gets the type of the fence.
     * @see {@link Type}.
     * @return the type of the fence.
     */
    @NonNull
    public Type getType() {
        return mType;
    }

    /**
     * Gets the pending intent class.
     * A fence has a pendingIntentClass only if it is the root fence that has been added to the
     * StorableFenceManager with
     * {@link StorableFenceManager#addFence(String, StorableFence, String)} or
     * {@link StorableFenceManager#addFence(String, StorableFence, HashMap, String)}.
     * @return the class name of the pending intent that will be called when the fence is fired
     */
    @Nullable
    public String getPendingIntentClass() {
        return mPendingIntentClass;
    }

    /**
     * Gets the additional data linked to this fence.
     * A fence has an additional data only if it is the root fence that has been added to the
     * StorableFenceManager with
     * {@link StorableFenceManager#addFence(String, StorableFence, HashMap, String)}.
     * @return a map of the additional data. Empty if it has not been set.
     */
    @NonNull
    public HashMap<String, Object> getAdditionalData() {
        return mAdditionalData;
    }

    AwarenessFence getAwarenessFence(Context ctx) {
        if (mType.equals(Type.META)) {
            if (!mAndFences.isEmpty()) {
                List<AwarenessFence> awarenessFences = new ArrayList<>();
                for (StorableFence subFence : mAndFences) {
                    awarenessFences.add(subFence.getAwarenessFence(ctx));
                }
                return AwarenessFence.and(awarenessFences);
            } else if (!mOrFences.isEmpty()) {
                List<AwarenessFence> awarenessFences = new ArrayList<>();
                for (StorableFence subFence : mOrFences) {
                    awarenessFences.add(subFence.getAwarenessFence(ctx));
                }
                return AwarenessFence.or(awarenessFences);
            } else if (mNotFence != null) {
                return AwarenessFence.not(mNotFence.getAwarenessFence(ctx));
            }
        }
        return null;
    }
    //endregion getters

    //region setters
    void setId(String id) {
        mId = id;
    }

    void setPendingIntentClass(String pendingIntentClass) {
        mPendingIntentClass = pendingIntentClass;
    }

    void setAdditionalData(@Nullable HashMap<String,Object> additionalData) {
        if (additionalData != null) {
            mAdditionalData.putAll(additionalData);
        }
    }

    //endregion setters


    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof StorableFence))return false;
        StorableFence fence = (StorableFence)other;
        return ((mType == fence.getType()) &&
                (mAdditionalData.equals(fence.getAdditionalData())) &&
                (mAndFences.equals(fence.getAndFences())) &&
                (mOrFences.equals(fence.getOrFences())) &&
                ((mNotFence == null && fence.getNotFence() == null) ||
                        ((mNotFence != null) && (mNotFence.equals(fence.getNotFence())))) &&
                ((mPendingIntentClass == null && fence.getPendingIntentClass() == null) ||
                        ((mPendingIntentClass != null) &&
                                (mPendingIntentClass.equals(fence.getPendingIntentClass()))))
        );
    }

    /**
     * Creates a meta storable fence which is a logical 'and' of all the given fences
     * @param fences the fences to be added to the 'and' list of the resulting fence
     * @return a meta fence that has 'and' fences
     */
    @NonNull
    public static StorableFence and(@NonNull StorableFence... fences) {
        return StorableFence.and(Arrays.asList(fences));
    }

    /**
     * Creates a meta storable fence which is a logical 'and' of all the given fences
     * @param fences the fences to be added to the 'and' list of the resulting fence
     * @return a meta fence that has 'and' fences
     */
    @NonNull
    public static StorableFence and(@NonNull Collection<StorableFence> fences) {
        // create a meta fence
        StorableFence metaFence = new StorableFence(Type.META);
        metaFence.mAndFences.addAll(fences);
        return metaFence;
    }

    /**
     * Creates a meta storable fence which is a logical 'or' of all the given fences
     * @param fences the fences to be added to the 'or' list of the resulting fence
     * @return a meta fence that has 'or' fences
     */
    @NonNull
    public static StorableFence or(@NonNull StorableFence... fences) {
        return StorableFence.or(Arrays.asList(fences));
    }

    /**
     * Creates a meta storable fence which is a logical 'or' of all the given fences
     * @param fences the fences to be added to the 'or' list of the resulting fence
     * @return a meta fence that has 'or' fences
     */
    @NonNull
    public static StorableFence or(@NonNull Collection<StorableFence> fences) {
        // create a meta fence
        StorableFence metaFence = new StorableFence(Type.META);
        metaFence.mOrFences.addAll(fences);
        return metaFence;
    }

    /**
     * Creates a meta storable fence which is a logical 'not' of the given fence
     * @param fence the fence to be negated
     * @return a meta fence that has a 'not' fence
     */
    @NonNull
    public static StorableFence not(@NonNull StorableFence fence) {
        // create a meta fence
        StorableFence metaFence = new StorableFence(Type.META);
        metaFence.mNotFence = fence;
        return metaFence;
    }

    static StorableFence stringToFence(String jsonStr) {
        try {
            JSONObject root = new JSONObject(jsonStr);
            return jsonToFence(root);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    static StorableFence jsonToFence(JSONObject jsonObj) {
        StorableFence fenceToReturn = null;
        try {
            StorableFence.Type type = StorableFence.Type.values()[jsonObj.getInt(FENCE_TYPE_KEY)];
            switch (type) {
                case META:
                    if (jsonObj.has(FENCE_META_AND_KEY)) {
                        JSONArray jsonArr = jsonObj.getJSONArray(FENCE_META_AND_KEY);
                        Collection<StorableFence> fenceArr = new ArrayList<>();
                        for(int i = 0; i < jsonArr.length(); i++) {
                            JSONObject jsonFence = jsonArr.getJSONObject(i);
                            fenceArr.add(jsonToFence(jsonFence));
                        }
                        fenceToReturn = StorableFence.and(fenceArr);
                    } else if (jsonObj.has(FENCE_META_OR_KEY)) {
                        JSONArray jsonArr = jsonObj.getJSONArray(FENCE_META_OR_KEY);
                        Collection<StorableFence> fenceArr = new ArrayList<>();
                        for(int i = 0; i < jsonArr.length(); i++) {
                            JSONObject jsonFence = jsonArr.getJSONObject(i);
                            fenceArr.add(jsonToFence(jsonFence));
                        }
                        fenceToReturn = StorableFence.or(fenceArr);

                    } else if (jsonObj.has(FENCE_META_NOT_KEY)) {
                        JSONObject jsonFence = jsonObj.getJSONObject(FENCE_META_NOT_KEY);
                        StorableFence fence = jsonToFence(jsonFence);
                        if (fence != null) {
                            fenceToReturn = StorableFence.not(fence);
                        }
                    }
                    break;
                case LOCATION:
                    fenceToReturn = StorableLocationFence.jsonToLocationFence(jsonObj);
                    break;
                case ACTIVITY:
                    fenceToReturn = StorableActivityFence.jsonToActivityFence(jsonObj);
                    break;
                case TIME:
                    fenceToReturn = StorableTimeFence.jsonToTimeFence(jsonObj);
                    break;
                case HEADPHONE:
                    fenceToReturn = StorableHeadphoneFence.jsonToHeadphoneFence(jsonObj);
                    break;
            }
            if (fenceToReturn!= null && jsonObj.has(FENCE_ID_KEY)) {
                fenceToReturn.mId = jsonObj.getString(FENCE_ID_KEY);
            }
            if (fenceToReturn!= null && jsonObj.has(FENCE_PENDING_INTENT_CLASS_KEY)) {
                fenceToReturn.mPendingIntentClass = jsonObj.getString(FENCE_PENDING_INTENT_CLASS_KEY);
            }
            if (fenceToReturn!= null && jsonObj.has(FENCE_ADDITIONAL_DATA_KEY)) {
                HashMap<String, Object> additionalData = new HashMap<>();
                JSONObject additionalDataObj = jsonObj.getJSONObject(FENCE_ADDITIONAL_DATA_KEY);
                Iterator keys = additionalDataObj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    JSONObject objAsJson = additionalDataObj.getJSONObject(key);
                    try {
                        Class classOfObj = Class.forName(objAsJson.getString(ADDITIONAL_DATA_TYPE_KEY));
                        additionalData.put(key, classOfObj.cast(objAsJson.get(ADDITIONAL_DATA_VALUE_KEY)));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                fenceToReturn.setAdditionalData(additionalData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fenceToReturn;
    }

    static String fenceToString(StorableFence fence) {
        JSONObject root = new JSONObject();
        fenceToString(fence, root);
        return root.toString();
    }

    static JSONObject fenceToString(StorableFence fence, JSONObject json) {
        try {
            if (fence.mId != null) {
                json.put(FENCE_ID_KEY, fence.mId);
            }
            if (fence.mPendingIntentClass != null) {
                json.put(FENCE_PENDING_INTENT_CLASS_KEY, fence.mPendingIntentClass);
            }
            if (!fence.getAdditionalData().isEmpty()) {
                JSONObject additionalDataAsJson = new JSONObject();
                Set<String> keys = fence.getAdditionalData().keySet();
                for (String key : keys) {
                    Object obj = fence.getAdditionalData().get(key);
                    if (obj != null) {
                        try {
                            Class objClass = obj.getClass();
                            if (objClass.equals(String.class) ||
                                    objClass.equals(Integer.class) ||
                                    objClass.equals(Long.class) ||
                                    objClass.equals(Double.class) ||
                                    objClass.equals(Boolean.class)) {
                                JSONObject objAsJson = new JSONObject();
                                objAsJson.put(ADDITIONAL_DATA_TYPE_KEY, objClass.getName());
                                objAsJson.put(ADDITIONAL_DATA_VALUE_KEY, obj);
                                additionalDataAsJson.put(key, objAsJson);
                            } else {
                                throw new Exception("Key " + key + " is storing a object of class " + objClass.toString() + " which is not supported");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                json.put(FENCE_ADDITIONAL_DATA_KEY, additionalDataAsJson);
            }
            switch (fence.mType) {
                case META:
                    json.put(FENCE_TYPE_KEY, StorableFence.Type.META.ordinal());

                    // if the meta fence is a AND meta
                    if (!fence.mAndFences.isEmpty()) {
                        JSONArray andArr = new JSONArray();
                        for (StorableFence subFence : fence.mAndFences) {
                            andArr.put(fenceToString(subFence, new JSONObject()));
                        }
                        json.put(FENCE_META_AND_KEY, andArr);
                    } else if (!fence.mOrFences.isEmpty()) {
                        JSONArray orArr = new JSONArray();
                        for (StorableFence subFence : fence.mOrFences) {
                            orArr.put(fenceToString(subFence, new JSONObject()));
                        }
                        json.put(FENCE_META_OR_KEY, orArr);
                    } else if (fence.mNotFence != null) {
                        json.put(FENCE_META_NOT_KEY, fenceToString(fence.mNotFence, new JSONObject()));
                    }
                    break;
                case ACTIVITY:
                    json = StorableActivityFence.activityFenceToString(fence, json);
                    break;
                case LOCATION:
                    json = StorableLocationFence.locationFenceToString(fence, json);
                    break;
                case TIME:
                    json = StorableTimeFence.timeFenceToString(fence, json);
                    break;
                case HEADPHONE:
                    json = StorableHeadphoneFence.headphoneFenceToString(fence, json);
                    break;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
