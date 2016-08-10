package com.sousoum.jcvd;

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

public class StorableActivityFence extends StorableFence {

    // TODO: do not use this enum, use DetectedActivityFence statics
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({IN_VEHICLE, ON_BICYCLE, ON_FOOT, STILL, UNKNOWN, TILTING, WALKING, RUNNING})
    public @interface ActivityType {}
    public static final int IN_VEHICLE = 0;
    public static final int ON_BICYCLE = 1;
    public static final int ON_FOOT = 2;
    public static final int STILL = 3;
    public static final int UNKNOWN = 4;
    public static final int TILTING = 5;
    public static final int WALKING = 7;
    public static final int RUNNING = 8;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({START_TYPE, STOP_TYPE, DURING_TYPE})
    public @interface TransitionType {}
    public static final int START_TYPE = 0;
    public static final int STOP_TYPE = 1;
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

    // TODO: check permission
    @SuppressWarnings("MissingPermission")
    @Override
    public AwarenessFence getAwarenessFence() {
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
    public int[] getActivityTypes() {
        return mActivityTypes;
    }

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
    //TODO @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    @SuppressWarnings("MissingPermission")
    public static StorableActivityFence starting(@ActivityType int... activityTypes) {
        return new StorableActivityFence(activityTypes, START_TYPE);
    }

    /**
     * Creates an storable activity fence which will be valid when the user stops one of the given
     * activity
     * @param activityTypes list of activities
     * @return an ActivityFence
     */
    //TODO @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    @SuppressWarnings("MissingPermission")
    public static StorableActivityFence stopping(@ActivityType int... activityTypes) {
        return new StorableActivityFence(activityTypes, STOP_TYPE);
    }

    /**
     * Creates an storable activity fence which will be valid when the user is doing one of the
     * given activity
     * @param activityTypes list of activities
     * @return an ActivityFence
     */
    //TODO @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    @SuppressWarnings("MissingPermission")
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

    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    static StorableFence jsonToActivityFence(JSONObject jsonObj) {
        try {
            if (jsonObj.getInt(FENCE_TYPE_KEY) == StorableFence.Type.ACTIVITY.ordinal()) {
                JSONArray jsonActivities = jsonObj.getJSONArray(ACTIVITIES_KEY);
                @StorableActivityFence.ActivityType int[] activities = new int[jsonActivities.length()];
                for(int i = 0; i < jsonActivities.length(); i++) {
                    @StorableActivityFence.ActivityType int activity = jsonActivities.getInt(i);
                    switch (activity) {
                        case StorableActivityFence.IN_VEHICLE:
                            activities[i] = StorableActivityFence.IN_VEHICLE;
                            break;
                        case StorableActivityFence.ON_BICYCLE:
                            activities[i] = StorableActivityFence.ON_BICYCLE;
                            break;
                        case StorableActivityFence.ON_FOOT:
                            activities[i] = StorableActivityFence.ON_FOOT;
                            break;
                        case StorableActivityFence.RUNNING:
                            activities[i] = StorableActivityFence.RUNNING;
                            break;
                        case StorableActivityFence.STILL:
                            activities[i] = StorableActivityFence.STILL;
                            break;
                        case StorableActivityFence.TILTING:
                            activities[i] = StorableActivityFence.TILTING;
                            break;
                        case StorableActivityFence.WALKING:
                            activities[i] = StorableActivityFence.WALKING;
                            break;
                        case StorableActivityFence.UNKNOWN:
                        default:
                            activities[i] = StorableActivityFence.UNKNOWN;
                            break;
                    }
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
