package com.sousoum.jcvd;

import android.content.Context;
import android.support.annotation.IntDef;

import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.TimeFence;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class StorableTimeFence extends StorableFence {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ABSOLUTE, DAILY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY})
    public @interface TimingType {}
    public static final int ABSOLUTE = 0;
    public static final int DAILY = 1;
    public static final int MONDAY = 2;
    public static final int TUESDAY = 3;
    public static final int WEDNESDAY = 4;
    public static final int THURSDAY = 5;
    public static final int FRIDAY = 7;
    public static final int SATURDAY = 8;
    public static final int SUNDAY = 9;

    @TimingType
    private final int mTimingType;

    private final TimeZone mTimeZone;

    private final long mStartTime;
    private final long mStopTime;

    private static final String TIMING_TYPE_KEY = "timing_type";
    private static final String TIMEZONE_OFFSET_KEY = "timezone_offset";
    private static final String TIMEZONE_ID_KEY = "timezone_id";
    private static final String START_TIME_KEY = "start";
    private static final String STOP_TIME_KEY = "stop";


    private StorableTimeFence(@TimingType int timingType, TimeZone timeZone, long startTime, long stopTime) {
        super(Type.TIME);
        mTimingType = timingType;
        mTimeZone = timeZone;
        mStartTime = startTime;
        mStopTime = stopTime;
    }

    @Override
    AwarenessFence getAwarenessFence(Context ctx) {
        switch (mTimingType) {
            case ABSOLUTE:
                return TimeFence.inInterval(mStartTime, mStopTime);
            case DAILY:
                return TimeFence.inDailyInterval(mTimeZone, mStartTime, mStopTime);
            case MONDAY:
                return TimeFence.inMondayInterval(mTimeZone, mStartTime, mStopTime);
            case TUESDAY:
                return TimeFence.inTuesdayInterval(mTimeZone, mStartTime, mStopTime);
            case WEDNESDAY:
                return TimeFence.inWednesdayInterval(mTimeZone, mStartTime, mStopTime);
            case THURSDAY:
                return TimeFence.inThursdayInterval(mTimeZone, mStartTime, mStopTime);
            case FRIDAY:
                return TimeFence.inFridayInterval(mTimeZone, mStartTime, mStopTime);
            case SATURDAY:
                return TimeFence.inSaturdayInterval(mTimeZone, mStartTime, mStopTime);
            case SUNDAY:
                return TimeFence.inSundayInterval(mTimeZone, mStartTime, mStopTime);
        }
        return null;
    }

    //region getters
    public int getTimingType() {
        return mTimingType;
    }

    public TimeZone getTimeZone() {
        return mTimeZone;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getStopTime() {
        return mStopTime;
    }
    //endregion getters

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof StorableTimeFence))return false;
        StorableTimeFence fence = (StorableTimeFence)other;
        return ((super.equals(other)) &&
                (mTimingType == fence.getTimingType()) &&
                ((mTimeZone == null) && (fence.getTimeZone() == null) ||
                        ((mTimeZone != null) && (fence.getTimeZone() != null) &&
                                (mTimeZone.getDisplayName().equals(fence.getTimeZone().getDisplayName())) &&
                                (mTimeZone.getID().equals(fence.getTimeZone().getID())))) &&
                (mStartTime == fence.getStartTime()) &&
                (mStopTime == fence.getStopTime())
        );
    }

    /**
     * Creates a storable time fence which will be valid in the given absolute time frame
     * @param startTimeMillis absolute start time in milli since epoch
     * @param stopTimeMillis absolute stop time in milli since epoch
     * @return a time fence
     */
    public static StorableTimeFence inInterval(long startTimeMillis, long stopTimeMillis) {
        return new StorableTimeFence(ABSOLUTE, null, startTimeMillis, stopTimeMillis);
    }

    /**
     * Creates a storable time fence which will be valid in the given daily time frame
     * @param timeZone the time zone to use
     * @param startTimeOfDayMillis Milliseconds since the start of the day. 12:00 am is 0L.
     *                             The maximum value is the number of milliseconds in a day,
     *                             namely 24L * 60L * 60L * 1000L.
     * @param stopTimeOfDayMillis milliseconds since the start of the day. Same range as
     *                            startTimeOfDayMillis. This time must be greater than or equal
     *                            to startTimeOfDayMillis.
     * @return a time fence
     */
    public static StorableTimeFence inDailyInterval(TimeZone timeZone, long startTimeOfDayMillis, long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAILY, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis);
    }

    /**
     * Creates a storable time fence which will be valid on Monday in the given daily time frame
     * @param timeZone the time zone to use
     * @param startTimeOfDayMillis Milliseconds since the start of the day. 12:00 am is 0L.
     *                             The maximum value is the number of milliseconds in a day,
     *                             namely 24L * 60L * 60L * 1000L.
     * @param stopTimeOfDayMillis milliseconds since the start of the day. Same range as
     *                            startTimeOfDayMillis. This time must be greater than or equal
     *                            to startTimeOfDayMillis.
     * @return a time fence
     */
    public static StorableTimeFence inMondayInterval(TimeZone timeZone, long startTimeOfDayMillis, long stopTimeOfDayMillis) {
        return new StorableTimeFence(MONDAY, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis);
    }

    /**
     * Creates a storable time fence which will be valid on Tuesday in the given daily time frame
     * @param timeZone the time zone to use
     * @param startTimeOfDayMillis Milliseconds since the start of the day. 12:00 am is 0L.
     *                             The maximum value is the number of milliseconds in a day,
     *                             namely 24L * 60L * 60L * 1000L.
     * @param stopTimeOfDayMillis milliseconds since the start of the day. Same range as
     *                            startTimeOfDayMillis. This time must be greater than or equal
     *                            to startTimeOfDayMillis.
     * @return a time fence
     */
    public static StorableTimeFence inTuesdayInterval(TimeZone timeZone, long startTimeOfDayMillis, long stopTimeOfDayMillis) {
        return new StorableTimeFence(TUESDAY, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis);
    }

    /**
     * Creates a storable time fence which will be valid on Wednesday in the given daily time frame
     * @param timeZone the time zone to use
     * @param startTimeOfDayMillis Milliseconds since the start of the day. 12:00 am is 0L.
     *                             The maximum value is the number of milliseconds in a day,
     *                             namely 24L * 60L * 60L * 1000L.
     * @param stopTimeOfDayMillis milliseconds since the start of the day. Same range as
     *                            startTimeOfDayMillis. This time must be greater than or equal
     *                            to startTimeOfDayMillis.
     * @return a time fence
     */
    public static StorableTimeFence inWednesdayInterval(TimeZone timeZone, long startTimeOfDayMillis, long stopTimeOfDayMillis) {
        return new StorableTimeFence(WEDNESDAY, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis);
    }

    /**
     * Creates a storable time fence which will be valid on Thursday in the given daily time frame
     * @param timeZone the time zone to use
     * @param startTimeOfDayMillis Milliseconds since the start of the day. 12:00 am is 0L.
     *                             The maximum value is the number of milliseconds in a day,
     *                             namely 24L * 60L * 60L * 1000L.
     * @param stopTimeOfDayMillis milliseconds since the start of the day. Same range as
     *                            startTimeOfDayMillis. This time must be greater than or equal
     *                            to startTimeOfDayMillis.
     * @return a time fence
     */
    public static StorableTimeFence inThursdayInterval(TimeZone timeZone, long startTimeOfDayMillis, long stopTimeOfDayMillis) {
        return new StorableTimeFence(THURSDAY, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis);
    }

    /**
     * Creates a storable time fence which will be valid on Friday in the given daily time frame
     * @param timeZone the time zone to use
     * @param startTimeOfDayMillis Milliseconds since the start of the day. 12:00 am is 0L.
     *                             The maximum value is the number of milliseconds in a day,
     *                             namely 24L * 60L * 60L * 1000L.
     * @param stopTimeOfDayMillis milliseconds since the start of the day. Same range as
     *                            startTimeOfDayMillis. This time must be greater than or equal
     *                            to startTimeOfDayMillis.
     * @return a time fence
     */
    public static StorableTimeFence inFridayInterval(TimeZone timeZone, long startTimeOfDayMillis, long stopTimeOfDayMillis) {
        return new StorableTimeFence(FRIDAY, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis);
    }

    /**
     * Creates a storable time fence which will be valid on Saturday in the given daily time frame
     * @param timeZone the time zone to use
     * @param startTimeOfDayMillis Milliseconds since the start of the day. 12:00 am is 0L.
     *                             The maximum value is the number of milliseconds in a day,
     *                             namely 24L * 60L * 60L * 1000L.
     * @param stopTimeOfDayMillis milliseconds since the start of the day. Same range as
     *                            startTimeOfDayMillis. This time must be greater than or equal
     *                            to startTimeOfDayMillis.
     * @return a time fence
     */
    public static StorableTimeFence inSaturdayInterval(TimeZone timeZone, long startTimeOfDayMillis, long stopTimeOfDayMillis) {
        return new StorableTimeFence(SATURDAY, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis);
    }

    /**
     * Creates a storable time fence which will be valid on Sunday in the given daily time frame
     * @param timeZone the time zone to use
     * @param startTimeOfDayMillis Milliseconds since the start of the day. 12:00 am is 0L.
     *                             The maximum value is the number of milliseconds in a day,
     *                             namely 24L * 60L * 60L * 1000L.
     * @param stopTimeOfDayMillis milliseconds since the start of the day. Same range as
     *                            startTimeOfDayMillis. This time must be greater than or equal
     *                            to startTimeOfDayMillis.
     * @return a time fence
     */
    public static StorableTimeFence inSundayInterval(TimeZone timeZone, long startTimeOfDayMillis, long stopTimeOfDayMillis) {
        return new StorableTimeFence(SUNDAY, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis);
    }

    static JSONObject timeFenceToString(StorableFence fence, JSONObject json) {
        if (fence.getType() == Type.TIME) {
            StorableTimeFence timeFence = (StorableTimeFence) fence;
            try {
                json.put(FENCE_TYPE_KEY, Type.TIME.ordinal());
                json.put(TIMING_TYPE_KEY, timeFence.mTimingType);
                if (timeFence.mTimeZone != null) {
                    json.put(TIMEZONE_OFFSET_KEY, timeFence.mTimeZone.getRawOffset());
                    json.put(TIMEZONE_ID_KEY, timeFence.mTimeZone.getID());
                }
                json.put(START_TIME_KEY, timeFence.getStartTime());
                json.put(STOP_TIME_KEY, timeFence.getStopTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return json;
    }

    static StorableFence jsonToTimeFence(JSONObject jsonObj) {
        try {
            if (jsonObj.getInt(FENCE_TYPE_KEY) == Type.TIME.ordinal()) {
                @TimingType int timingType = jsonObj.getInt(TIMING_TYPE_KEY);
                TimeZone timeZone = null;
                if (jsonObj.has(TIMEZONE_OFFSET_KEY) && jsonObj.has(TIMEZONE_ID_KEY)) {
                    timeZone = new SimpleTimeZone(jsonObj.getInt(TIMEZONE_OFFSET_KEY), jsonObj.getString(TIMEZONE_ID_KEY));
                }
                long startTime = jsonObj.getLong(START_TIME_KEY);
                long stopTime = jsonObj.getLong(STOP_TIME_KEY);
                switch (timingType) {
                    case StorableTimeFence.ABSOLUTE:
                        return StorableTimeFence.inInterval(startTime, stopTime);
                    case StorableTimeFence.DAILY:
                        return StorableTimeFence.inDailyInterval(timeZone, startTime, stopTime);
                    case StorableTimeFence.MONDAY:
                        return StorableTimeFence.inMondayInterval(timeZone, startTime, stopTime);
                    case StorableTimeFence.TUESDAY:
                        return StorableTimeFence.inTuesdayInterval(timeZone, startTime, stopTime);
                    case StorableTimeFence.WEDNESDAY:
                        return StorableTimeFence.inWednesdayInterval(timeZone, startTime, stopTime);
                    case StorableTimeFence.THURSDAY:
                        return StorableTimeFence.inThursdayInterval(timeZone, startTime, stopTime);
                    case StorableTimeFence.FRIDAY:
                        return StorableTimeFence.inFridayInterval(timeZone, startTime, stopTime);
                    case StorableTimeFence.SATURDAY:
                        return StorableTimeFence.inSaturdayInterval(timeZone, startTime, stopTime);
                    case StorableTimeFence.SUNDAY:
                        return StorableTimeFence.inSundayInterval(timeZone, startTime, stopTime);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
