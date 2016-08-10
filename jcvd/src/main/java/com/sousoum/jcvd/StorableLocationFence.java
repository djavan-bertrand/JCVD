package com.sousoum.jcvd;

import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.LocationFence;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StorableLocationFence extends StorableFence {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ENTER_TYPE, EXIT_TYPE, IN_TYPE})
    public @interface TransitionType {}
    public static final int ENTER_TYPE = 0;
    public static final int EXIT_TYPE = 1;
    public static final int IN_TYPE = 2;

    private double mLatitude;
    private double mLongitude;
    private double mRadius;
    private long mDwellTimeMillis;

    private static final String TRANSITION_TYPE_KEY = "transition";
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";
    private static final String RADIUS_KEY = "radius";
    private static final String DWELL_KEY = "dwell";

    @TransitionType
    private int mTransitionType;

    private StorableLocationFence(@TransitionType int transitionType, double latitude,
                                    double longitude, double radius, long dwellTimeMillis) {
        super(Type.LOCATION);
        mTransitionType = transitionType;
        mLatitude = latitude;
        mLongitude = longitude;
        mRadius = radius;
        mDwellTimeMillis = dwellTimeMillis;
    }

    //TODO: check permission
    @SuppressWarnings("MissingPermission")
    @Override
    public AwarenessFence getAwarenessFence() {
        switch (mTransitionType) {
            case ENTER_TYPE:
                return LocationFence.entering(mLatitude, mLongitude, mRadius);
            case EXIT_TYPE:
                return LocationFence.exiting(mLatitude, mLongitude, mRadius);
            case IN_TYPE:
                return LocationFence.in(mLatitude, mLongitude, mRadius, mDwellTimeMillis);
        }

        return null;
    }

    //region getters
    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getRadius() {
        return mRadius;
    }

    public long getDwellTimeMillis() {
        return mDwellTimeMillis;
    }

    public int getTransitionType() {
        return mTransitionType;
    }
    //endregion getters

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof StorableLocationFence))return false;
        StorableLocationFence fence = (StorableLocationFence)other;
        return ((super.equals(other)) &&
                (mTransitionType == fence.getTransitionType()) &&
                (mLatitude == fence.getLatitude()) &&
                (mLongitude == fence.getLongitude()) &&
                (mRadius == fence.getRadius()) &&
                (mDwellTimeMillis == fence.getDwellTimeMillis())
        );
    }

    /**
     * Creates a storable location fence which will be valid when the user enter the given region
     * @param latitude the latitude of the center of the region
     * @param longitude the longitude of the center of the region
     * @param radius the radius of the region (in meter)
     * @return a location fence
     */
    //TODO @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public static StorableLocationFence entering(double latitude, double longitude, double radius) {
        //noinspection MissingPermission
        return new StorableLocationFence(ENTER_TYPE, latitude, longitude, radius, 0);
    }

    /**
     * Creates a storable location fence which will be valid when the user leaves the given region
     * @param latitude the latitude of the center of the region
     * @param longitude the longitude of the center of the region
     * @param radius the radius of the region (in meter)
     * @return a location fence
     */
    //TODO @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public static StorableLocationFence exiting(double latitude, double longitude, double radius) {
        //noinspection MissingPermission
        return new StorableLocationFence(EXIT_TYPE, latitude, longitude, radius, 0);
    }

    /**
     * Creates a storable location fence which will be valid when the user dwells in the given
     * region
     * @param latitude the latitude of the center of the region
     * @param longitude the longitude of the center of the region
     * @param radius the radius of the region (in meter)
     * @param dwellTimeMillis the minimum time in milli to consider a dwell
     * @return a location fence
     */
    //TODO @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public static StorableLocationFence in(double latitude, double longitude, double radius,
                                           long dwellTimeMillis) {
        //noinspection MissingPermission
        return new StorableLocationFence(IN_TYPE, latitude, longitude, radius, dwellTimeMillis);
    }

    //TODO @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    static StorableFence jsonToLocationFence(JSONObject jsonObj) {
        try {
            if (jsonObj.getInt(FENCE_TYPE_KEY) == StorableFence.Type.LOCATION.ordinal()) {
                int transition = jsonObj.getInt(TRANSITION_TYPE_KEY);
                double latitude = jsonObj.getDouble(LATITUDE_KEY);
                double longitude = jsonObj.getDouble(LONGITUDE_KEY);
                double radius = jsonObj.getDouble(RADIUS_KEY);
                long dwell = jsonObj.getLong(DWELL_KEY);
                switch (transition) {
                    case StorableLocationFence.ENTER_TYPE:
                        //noinspection MissingPermission
                        return StorableLocationFence.entering(latitude, longitude, radius);
                    case StorableLocationFence.EXIT_TYPE:
                        //noinspection MissingPermission
                        return StorableLocationFence.exiting(latitude, longitude, radius);
                    case StorableLocationFence.IN_TYPE:
                        //noinspection MissingPermission
                        return StorableLocationFence.in(latitude, longitude, radius, dwell);
                    default:
                        Log.e("LocationFence", "not normal");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject locationFenceToString(StorableFence fence, JSONObject json) {
        if (fence.getType() == StorableFence.Type.LOCATION) {
            StorableLocationFence locFence = (StorableLocationFence) fence;
            try {
                json.put(FENCE_TYPE_KEY, StorableFence.Type.LOCATION.ordinal());
                json.put(TRANSITION_TYPE_KEY, locFence.mTransitionType);
                json.put(LATITUDE_KEY, locFence.mLatitude);
                json.put(LONGITUDE_KEY, locFence.mLongitude);
                json.put(RADIUS_KEY, locFence.mRadius);
                json.put(DWELL_KEY, locFence.mDwellTimeMillis);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return json;
    }
}
