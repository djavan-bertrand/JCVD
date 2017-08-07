package com.sousoum.jcvd;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

/**
 * A storable fence that backs up a {@link DetectedActivityFence}.
 * This fence will be true when one of the given activity in the list of changes to the expected
 * state.
 *
 * You can get the list of activities with {@link StorableActivityFence#getActivityTypes()}.
 * You can get the expected state with {@link StorableActivityFence#getTransitionType()}.
 * Note: Fence is momentarily TRUE for about 5 seconds on the state change,
 * then automatically revert to FALSE.
 */
public final class StorableActivityFence extends StorableFence {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DetectedActivityFence.IN_VEHICLE, DetectedActivityFence.ON_BICYCLE,
            DetectedActivityFence.ON_FOOT, DetectedActivityFence.STILL,
            DetectedActivityFence.UNKNOWN, DetectedActivityFence.WALKING,
            DetectedActivityFence.RUNNING})
    public @interface ActivityType {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({START_TYPE, STOP_TYPE, DURING_TYPE})
    public @interface TransitionType {}

    /**
     * With this transition, the fence is momentarily (about 5 seconds) TRUE when the user begins
     * to engage in one of the activityTypes and the previous activity was not one of the values in
     * activityTypes.
     */
    public static final int START_TYPE = 0;

    /**
     * With this transition, the fence is momentarily (about 5 seconds) TRUE when the user stops
     * one of the activityTypes, and transitions to an activity that is not in activityTypes.
     */
    public static final int STOP_TYPE = 1;

    /**
     * With this transition, the fence is in the TRUE state when the user is currently engaged in
     * one of the specified activityTypes, and FALSE otherwise.
     */
    public static final int DURING_TYPE = 2;

    @ActivityType
    private int[] mActivityTypes;

    @TransitionType
    private int mTransitionType;

    private static final String ACTIVITIES_KEY = "activities";
    private static final String TRANSITION_TYPE_KEY = "transition";

    private StorableActivityFence(@ActivityType int[] activityTypes, int transitionType) {
        super(Type.ACTIVITY);
        mActivityTypes = activityTypes;
        mTransitionType = transitionType;
    }

    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    @Override
    AwarenessFence getAwarenessFence(Context ctx) {
        switch (mTransitionType) {
            case DURING_TYPE:
                return DetectedActivityFence.during(mActivityTypes);
            case START_TYPE:
                return DetectedActivityFence.starting(mActivityTypes);
            case STOP_TYPE:
                return DetectedActivityFence.stopping(mActivityTypes);
        }
        return null;
    }

    //region getters
    /**
     * Get the list of activity types concerned by this fence.
     * @return an array of activities
     * @see {@link ActivityType}
     */
    public int[] getActivityTypes() {
        return mActivityTypes;
    }

    /**
     * Get the transition type of the fence
     * @return the transition type
     * @see {@link TransitionType}
     */
    @TransitionType
    public int getTransitionType() {
        return mTransitionType;
    }
    //endregion getters

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof StorableActivityFence))return false;
        StorableActivityFence fence = (StorableActivityFence)other;
        return ((super.equals(other)) &&
                (mTransitionType == fence.getTransitionType()) &&
                (Arrays.equals(mActivityTypes,fence.getActivityTypes()))
        );
    }

    /**
     * Creates an storable activity fence which will be valid when the user starts one of the given
     * activity
     * @param activityTypes list of activities
     * @return an ActivityFence
     */
    public static StorableActivityFence starting(@ActivityType int... activityTypes) {
        return new StorableActivityFence(activityTypes, START_TYPE);
    }

    /**
     * Creates an storable activity fence which will be valid when the user stops one of the given
     * activity
     * @param activityTypes list of activities
     * @return an ActivityFence
     */
    public static StorableActivityFence stopping(@ActivityType int... activityTypes) {
        return new StorableActivityFence(activityTypes, STOP_TYPE);
    }

    /**
     * Creates an storable activity fence which will be valid when the user is doing one of the
     * given activity
     * @param activityTypes list of activities
     * @return an ActivityFence
     */
    public static StorableActivityFence during(@ActivityType int... activityTypes) {
        return new StorableActivityFence(activityTypes, DURING_TYPE);
    }

    static JSONObject activityFenceToString(StorableFence fence, JSONObject json) {
        if (fence.getType() == StorableFence.Type.ACTIVITY) {
            StorableActivityFence actFence = (StorableActivityFence) fence;
            JSONArray activityArr = new JSONArray();
            for (int activity : actFence.mActivityTypes) {
                activityArr.put(activity);
            }
            try {
                json.put(FENCE_TYPE_KEY, StorableFence.Type.ACTIVITY.ordinal());
                json.put(TRANSITION_TYPE_KEY, actFence.mTransitionType);
                json.put(ACTIVITIES_KEY, activityArr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return json;
    }

    static StorableFence jsonToActivityFence(JSONObject jsonObj) {
        try {
            if (jsonObj.getInt(FENCE_TYPE_KEY) == StorableFence.Type.ACTIVITY.ordinal()) {
                JSONArray jsonActivities = jsonObj.getJSONArray(ACTIVITIES_KEY);
                @StorableActivityFence.ActivityType int[] activities = new int[jsonActivities.length()];
                for(int i = 0; i < jsonActivities.length(); i++) {
                    @StorableActivityFence.ActivityType int activity = jsonActivities.getInt(i);
                    activities[i] = activity;
                }
                int transition = jsonObj.getInt(TRANSITION_TYPE_KEY);
                switch (transition) {
                    case StorableActivityFence.START_TYPE:
                        return StorableActivityFence.starting(activities);
                    case StorableActivityFence.STOP_TYPE:
                        return StorableActivityFence.stopping(activities);
                    case StorableActivityFence.DURING_TYPE:
                        return StorableActivityFence.during(activities);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
