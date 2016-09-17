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

/**
 * A storable fence that backs up a {@link TimeFence}.
 * This fence will be true during the given time frame.
 *
 * You can get the type of timing with {@link StorableTimeFence#getTimingType()}
 * You can get the time zone used with {@link StorableTimeFence#getTimeZone()} ()}.
 * You can get the start time with {@link StorableTimeFence#getStartTime()} ()}.
 * You can get the stop time with {@link StorableTimeFence#getStopTime()}.
 */
public final class StorableTimeFence extends StorableFence {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ABSOLUTE, DAILY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY})
    public @interface TimingType {}

    /**
     * With this type, the fence is in the TRUE state when the current time is within the absolute
     * times indicated by {@link StorableTimeFence#getStartTime()} and
     * {@link StorableTimeFence#getStopTime()}.
     */
    public static final int ABSOLUTE = 0;

    /**
     * With this type, the fence is in the TRUE state during the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     */
    public static final int DAILY = 1;

    /**
     * With this type, the fence is in the TRUE state for Monday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     */
    public static final int MONDAY = 2;

    /**
     * With this type, the fence is in the TRUE state for Tuesday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     */
    public static final int TUESDAY = 3;

    /**
     * With this type, the fence is in the TRUE state for Wednesday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     */
    public static final int WEDNESDAY = 4;

    /**
     * With this type, the fence is in the TRUE state for Thursday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     */
    public static final int THURSDAY = 5;

    /**
     * With this type, the fence is in the TRUE state for Friday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     */
    public static final int FRIDAY = 7;

    /**
     * With this type, the fence is in the TRUE state for Saturday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     */
    public static final int SATURDAY = 8;

    /**
     * With this type, the fence is in the TRUE state for Sunday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     */
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

    /**
     * Get the timing type
     * @return the timing type
     * @see {@link TimingType}
     */
    @TimingType
    public int getTimingType() {
        return mTimingType;
    }

    /**
     * Get the time zone used to understand the start and stop times
     * @return the timezone
     */
    public TimeZone getTimeZone() {
        return mTimeZone;
    }

    /**
     * Get the start time
     * @return the start time in milliseconds
     */
    public long getStartTime() {
        return mStartTime;
    }

    /**
     * Get the stop time
     * @return the stop time in milliseconds
     */
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
