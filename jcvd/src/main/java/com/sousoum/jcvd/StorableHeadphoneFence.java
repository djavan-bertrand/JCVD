package com.sousoum.jcvd;

import android.content.Context;
import android.support.annotation.IntDef;

import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.HeadphoneFence;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StorableHeadphoneFence extends StorableFence {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE, PLUGGING_IN, UNPLUGGING})
    public @interface TriggerType {}
    public static final int STATE = 0;
    public static final int PLUGGING_IN = 1;
    public static final int UNPLUGGING = 2;

    @TriggerType
    private final int mTriggerType;

    private final int mHeadphoneState;

    private static final String TRIGGER_TYPE_KEY = "trigger_type";
    private static final String HEADPHONE_STATE_KEY = "headphone_state";


    /**
     * Constructor for trigger type PLUGGING_IN or UNPLUGGING
     * @param triggerType the trigger type
     */
    private StorableHeadphoneFence(@TriggerType int triggerType) {
        super(Type.HEADPHONE);
        mTriggerType = triggerType;
        mHeadphoneState = 0;
    }

    /**
     * Constructor for trigger type STATE
     * @param triggerType the trigger type
     */
    private StorableHeadphoneFence(@TriggerType int triggerType, int headphoneState) {
        super(Type.HEADPHONE);
        mTriggerType = triggerType;
        mHeadphoneState = headphoneState;
    }

    @Override
    AwarenessFence getAwarenessFence(Context ctx) {
        switch (mTriggerType) {
            case STATE:
                return HeadphoneFence.during(mHeadphoneState);
            case PLUGGING_IN:
                return HeadphoneFence.pluggingIn();
            case UNPLUGGING:
                return HeadphoneFence.unplugging();
        }
        return null;
    }

    //region getters

    /**
     * Gets the trigger type of the fence
     * @return the type
     * @see {@link TriggerType}
     */
    @TriggerType
    public int getTriggerType() {
        return mTriggerType;
    }

    /**
     * Gets the headphone state required so that the fence is true
     * Only valid if the trigger type is {@link StorableHeadphoneFence#STATE}
     * @return the headphone state
     * @see {@link TriggerType}
     */
    public int getHeadphoneState() {
        return mHeadphoneState;
    }
    //endregion getters

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof StorableHeadphoneFence))return false;
        StorableHeadphoneFence fence = (StorableHeadphoneFence)other;
        return ((super.equals(other)) &&
                (mTriggerType == fence.getTriggerType()) &&
                (mHeadphoneState == fence.getHeadphoneState()));
    }

    /**
     * Creates a storable headphone fence which will be valid when the headphones are in
     * the specified state
     * @param state state of the headphone (
     *              {@link com.google.android.gms.awareness.state.HeadphoneState#PLUGGED_IN} or
     *              {@link com.google.android.gms.awareness.state.HeadphoneState#UNPLUGGED})
     * @return a headphone fence
     */
    public static StorableHeadphoneFence during(int state) {
        return new StorableHeadphoneFence(STATE, state);
    }

    /**
     * Creates a storable headphone fence which will be valid (during around 5seconds) when
     * headphones are plugged in to the device
     * @return a headphone fence
     */
    public static StorableHeadphoneFence pluggingIn() {
        return new StorableHeadphoneFence(PLUGGING_IN);
    }

    /**
     * Creates a storable headphone fence which will be valid (during around 5seconds) when
     * headphones unplugged from the device
     * @return a headphone fence
     */
    public static StorableHeadphoneFence unplugging() {
        return new StorableHeadphoneFence(UNPLUGGING);
    }

    static JSONObject headphoneFenceToString(StorableFence fence, JSONObject json) {
        if (fence.getType() == Type.HEADPHONE) {
            StorableHeadphoneFence headphoneFence = (StorableHeadphoneFence) fence;
            try {
                json.put(FENCE_TYPE_KEY, Type.HEADPHONE.ordinal());
                json.put(TRIGGER_TYPE_KEY, headphoneFence.mTriggerType);
                if (headphoneFence.mTriggerType == STATE) {
                    json.put(HEADPHONE_STATE_KEY, headphoneFence.mHeadphoneState);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return json;
    }

    static StorableFence jsonToHeadphoneFence(JSONObject jsonObj) {
        try {
            if (jsonObj.getInt(FENCE_TYPE_KEY) == Type.HEADPHONE.ordinal()) {
                @TriggerType int triggerType = jsonObj.getInt(TRIGGER_TYPE_KEY);
                switch (triggerType) {
                    case StorableHeadphoneFence.STATE:
                        int headphoneState = jsonObj.getInt(HEADPHONE_STATE_KEY);
                        return StorableHeadphoneFence.during(headphoneState);
                    case StorableHeadphoneFence.PLUGGING_IN:
                        return StorableHeadphoneFence.pluggingIn();
                    case StorableHeadphoneFence.UNPLUGGING:
                        return StorableHeadphoneFence.unplugging();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
