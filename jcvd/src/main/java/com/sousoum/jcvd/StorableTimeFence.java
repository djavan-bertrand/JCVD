package com.sousoum.jcvd;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.TimeFence;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static com.google.android.gms.awareness.fence.TimeFence.DAY_OF_WEEK_FRIDAY;
import static com.google.android.gms.awareness.fence.TimeFence.DAY_OF_WEEK_MONDAY;
import static com.google.android.gms.awareness.fence.TimeFence.DAY_OF_WEEK_SATURDAY;
import static com.google.android.gms.awareness.fence.TimeFence.DAY_OF_WEEK_SUNDAY;
import static com.google.android.gms.awareness.fence.TimeFence.DAY_OF_WEEK_THURSDAY;
import static com.google.android.gms.awareness.fence.TimeFence.DAY_OF_WEEK_TUESDAY;
import static com.google.android.gms.awareness.fence.TimeFence.DAY_OF_WEEK_WEDNESDAY;
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INSTANT_SUNRISE;
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INSTANT_SUNSET;
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INTERVAL_AFTERNOON;
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INTERVAL_EVENING;
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INTERVAL_HOLIDAY;
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INTERVAL_MORNING;
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INTERVAL_NIGHT;
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INTERVAL_WEEKDAY;
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INTERVAL_WEEKEND;

/**
 * A storable fence that backs up a {@link TimeFence}.
 * This fence will be true during the given time frame.
 *
 * You can get the type of timing with {@link StorableTimeFence#getTimingType()}
 * Then, according to this type of timing, you can get the other parameters.
 */
public final class StorableTimeFence extends StorableFence {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DAY_OF_WEEK_MONDAY, DAY_OF_WEEK_TUESDAY, DAY_OF_WEEK_WEDNESDAY, DAY_OF_WEEK_THURSDAY,
            DAY_OF_WEEK_FRIDAY, DAY_OF_WEEK_SATURDAY, DAY_OF_WEEK_SUNDAY})
    public @interface DayOfWeek {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TIME_INTERVAL_AFTERNOON, TIME_INTERVAL_EVENING, TIME_INTERVAL_HOLIDAY,
            TIME_INTERVAL_MORNING, TIME_INTERVAL_NIGHT, TIME_INTERVAL_WEEKDAY,
            TIME_INTERVAL_WEEKEND})
    public @interface TimeInterval {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TIME_INSTANT_SUNRISE, TIME_INSTANT_SUNSET})
    public @interface TimeInstant {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ABSOLUTE, DAILY, DAY_OF_WEEK, TIME_INTERVAL, TIME_INSTANT, MONDAY, TUESDAY, WEDNESDAY,
            THURSDAY, FRIDAY, SATURDAY, SUNDAY})
    public @interface TimingType {
    }

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
     * With this type, the fence is in the TRUE state on the
     * {@link StorableTimeFence#getDayOfWeek() day of the week} during the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     */
    public static final int DAY_OF_WEEK = 10;

    /**
     * With this type, the fence is in the TRUE state on the
     * {@link StorableTimeFence#getTimeInterval() time interval }.
     */
    public static final int TIME_INTERVAL = 11;

    /**
     * With this type, the fence is in the TRUE state on the
     * {@link StorableTimeFence#getTimeInstant() time instant} minus
     * {@link StorableTimeFence#getStartOffset()} and plus
     * {@link StorableTimeFence#getStopOffset()} as an offset.
     */
    public static final int TIME_INSTANT = 12;

    /**
     * With this type, the fence is in the TRUE state for Monday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     * @deprecated
     */
    public static final int MONDAY = 2;

    /**
     * With this type, the fence is in the TRUE state for Tuesday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     * @deprecated
     */
    public static final int TUESDAY = 3;

    /**
     * With this type, the fence is in the TRUE state for Wednesday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     * @deprecated
     */
    public static final int WEDNESDAY = 4;

    /**
     * With this type, the fence is in the TRUE state for Thursday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     * @deprecated
     */
    public static final int THURSDAY = 5;

    /**
     * With this type, the fence is in the TRUE state for Friday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     * @deprecated
     */
    public static final int FRIDAY = 7;

    /**
     * With this type, the fence is in the TRUE state for Saturday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     * @deprecated
     */
    public static final int SATURDAY = 8;

    /**
     * With this type, the fence is in the TRUE state for Sunday in the interval specified by
     * {@link StorableTimeFence#getStartTime()} as a startTimeOfDayMillis and
     * {@link StorableTimeFence#getStopTime()} as a stopTimeOfDayMillis in the given timeZone.
     * @deprecated
     */
    public static final int SUNDAY = 9;

    @TimingType
    private final int mTimingType;

    /** Day of the week. Only accurate if timing type is DAY_OF_WEEK. */
    @DayOfWeek
    private final int mDayOfWeek;

    /** Time interval. Not accurate if timing type is not TIME_INTERVAL. */
    @TimeInterval
    private final int mTimeInterval;

    /** Time instant. Not accurate if timing type is not DAILY or DAY_OF_WEEK. */
    @TimeInstant
    private final int mTimeInstant;

    /** Time zone. Not accurate if timing type is not TIME_INSTANT. */
    @Nullable
    private final TimeZone mTimeZone;

    /** Start time in millis. Not accurate if timing type is not ABSOLUTE, DAILY or DAY_OF_WEEK. */
    private final long mStartTime;
    /** Stop time in millis. Not accurate if timing type is not ABSOLUTE, DAILY or DAY_OF_WEEK. */
    private final long mStopTime;

    /** Start offset in millis. Not accurate if timing type is not TIME_INSTANT. */
    private final long mStartOffset;
    /** Stop offset in millis. Not accurate if timing type is not TIME_INSTANT. */
    private final long mStopOffset;

    private static final String TIMING_TYPE_KEY = "timing_type";
    private static final String DAY_OF_WEEK_KEY = "day_of_week";
    private static final String TIME_INTERVAL_KEY = "time_interval";
    private static final String TIME_INSTANT_KEY = "time_instant";
    private static final String TIMEZONE_OFFSET_KEY = "timezone_offset";
    private static final String TIMEZONE_ID_KEY = "timezone_id";
    private static final String START_TIME_KEY = "start";
    private static final String STOP_TIME_KEY = "stop";
    private static final String START_OFFSET_KEY = "start_offset";
    private static final String STOP_OFFSET_KEY = "stop_offset";


    private StorableTimeFence(@TimingType int timingType, @DayOfWeek int dayOfWeek,
                              @TimeInterval int timeInterval, @TimeInstant int timeInstant,
                              @Nullable TimeZone timeZone,
                              long startTime, long stopTime,
                              long startOffset, long stopOffset) {
        super(Type.TIME);
        mTimingType = timingType;
        mDayOfWeek = dayOfWeek;
        mTimeInterval = timeInterval;
        mTimeInstant = timeInstant;
        mTimeZone = timeZone;
        mStartTime = startTime;
        mStopTime = stopTime;
        mStartOffset = startOffset;
        mStopOffset = stopOffset;
    }

    @Override
    AwarenessFence getAwarenessFence(Context ctx) {
        switch (mTimingType) {
            case ABSOLUTE:
                return TimeFence.inInterval(mStartTime, mStopTime);
            case DAILY:
                return TimeFence.inDailyInterval(mTimeZone, mStartTime, mStopTime);
            case DAY_OF_WEEK:
                return TimeFence.inIntervalOfDay(mDayOfWeek, mTimeZone, mStartTime, mStopTime);
            case TIME_INTERVAL:
                if (ActivityCompat.checkSelfPermission(ctx,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    return TimeFence.inTimeInterval(mTimeInterval);
                }
                break;
            case TIME_INSTANT:
                return TimeFence.aroundTimeInstant(mTimeInstant, mStartOffset, mStopOffset);
            case MONDAY:
                return TimeFence.inIntervalOfDay(DAY_OF_WEEK_MONDAY, mTimeZone, mStartTime, mStopTime);
            case TUESDAY:
                return TimeFence.inIntervalOfDay(DAY_OF_WEEK_TUESDAY, mTimeZone, mStartTime, mStopTime);
            case WEDNESDAY:
                return TimeFence.inIntervalOfDay(DAY_OF_WEEK_WEDNESDAY, mTimeZone, mStartTime, mStopTime);
            case THURSDAY:
                return TimeFence.inIntervalOfDay(DAY_OF_WEEK_THURSDAY, mTimeZone, mStartTime, mStopTime);
            case FRIDAY:
                return TimeFence.inIntervalOfDay(DAY_OF_WEEK_FRIDAY, mTimeZone, mStartTime, mStopTime);
            case SATURDAY:
                return TimeFence.inIntervalOfDay(DAY_OF_WEEK_SATURDAY, mTimeZone, mStartTime, mStopTime);
            case SUNDAY:
                return TimeFence.inIntervalOfDay(DAY_OF_WEEK_SUNDAY, mTimeZone, mStartTime, mStopTime);

        }
        return null;
    }

    //region getters

    /**
     * Get the timing type
     * @return the timing type
     */
    @TimingType
    public int getTimingType() {
        return mTimingType;
    }

    /**
     * Get the day of the week.
     * Only accurate if timing type is {@link StorableTimeFence#DAY_OF_WEEK}.
     * @return the day of the week when this time fence is triggered
     */
    public int getDayOfWeek() {
        return mDayOfWeek;
    }

    /**
     * Get the time interval.
     * Only accurate if timing type is {@link StorableTimeFence#TIME_INTERVAL}.
     * @return the time interval when this time fence is triggered
     */
    public int getTimeInterval() {
        return mTimeInterval;
    }

    /**
     * Get the time instant.
     * Only accurate if timing type is {@link StorableTimeFence#TIME_INSTANT}.
     * @return the time instant when this time fence is triggered
     */
    public int getTimeInstant() {
        return mTimeInstant;
    }

    /**
     * Get the start offset in millis.
     * Only accurate if timing type is {@link StorableTimeFence#TIME_INSTANT}.
     * @return the offset from the beginning of the semantic time period
     */
    public long getStartOffset() {
        return mStartOffset;
    }

    /**
     * Get the stop offset in millis.
     * Only accurate if timing type is {@link StorableTimeFence#TIME_INSTANT}.
     * @return the offset from the end of the semantic time period
     */
    public long getStopOffset() {
        return mStopOffset;
    }

    /**
     * Get the time zone used to understand the start and stop times
     * @return the timezone
     */
    @Nullable
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
                (mTimeInterval == fence.getTimeInterval()) &&
                (mTimeInstant == fence.getTimeInstant()) &&
                ((mTimeZone == null) && (fence.getTimeZone() == null) ||
                        ((mTimeZone != null) && (fence.getTimeZone() != null) &&
                                (mTimeZone.getDisplayName().equals(fence.getTimeZone().getDisplayName())) &&
                                (mTimeZone.getID().equals(fence.getTimeZone().getID())))) &&
                (mStartTime == fence.getStartTime()) &&
                (mStopTime == fence.getStopTime()) &&
                (mStartOffset == fence.getStartOffset()) &&
                (mStopOffset == fence.getStopOffset())
        );
    }

    /**
     * Creates a storable time fence which will be valid on dayOfWeek during the interval
     * specified by startTimeOfDayMillis to stopTimeOfDayMillis in the given timeZone.
     * @param dayOfWeek the day of the week
     * @param timeZone the time zone to use. If null current device time zone is used.
     * @param startTimeOfDayMillis Milliseconds since the start of the day.
     * @param stopTimeOfDayMillis Milliseconds since the start of the day. This time must be
     *                            greater than or equal to startTimeOfDayMillis.
     * @return a time fence
     */
    @NonNull
    public static StorableTimeFence inIntervalOfDay(@DayOfWeek int dayOfWeek,
                                                    @Nullable TimeZone timeZone,
                                                    long startTimeOfDayMillis,
                                                    long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAY_OF_WEEK, dayOfWeek, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, timeZone, startTimeOfDayMillis,
                stopTimeOfDayMillis, 0, 0);
    }



    /**
     * Creates a storable time fence which will be valid in the given absolute time frame
     * @param startTimeMillis absolute start time in milli since epoch
     * @param stopTimeMillis absolute stop time in milli since epoch
     * @return a time fence
     */
    @NonNull
    public static StorableTimeFence inInterval(long startTimeMillis, long stopTimeMillis) {
        return new StorableTimeFence(ABSOLUTE, DAY_OF_WEEK_SUNDAY, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, null, startTimeMillis, stopTimeMillis, 0, 0);
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
    @NonNull
    public static StorableTimeFence inDailyInterval(@Nullable TimeZone timeZone,
                                                    long startTimeOfDayMillis,
                                                    long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAILY, DAY_OF_WEEK_SUNDAY, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis, 0, 0);
    }

    /**
     * Creates a storable time fence which will be valid in the given daily time frame
     * @param timeInterval the time interval to use
     * @return a time fence
     */
    @NonNull
    public static StorableTimeFence inTimeInterval(@TimeInterval int timeInterval) {
        return new StorableTimeFence(TIME_INTERVAL, DAY_OF_WEEK_SUNDAY, timeInterval,
                TIME_INSTANT_SUNRISE, null, 0, 0, 0, 0);
    }

    /**
     * Creates a storable time fence which will be valid in the given daily time frame
     * @param timeInstant the desired semantic time label around which fence triggers are defined
     *                    to happen.
     * @param startOffsetMillis offset from the beginning of the semantic time period.
     *                          It can be specified as a positive or negative offset value but
     *                          should be between -24 to 24 hours inclusive (expressed in millis)
     * @param stopOffsetMillis offset from the end of the semantic time period. It can be specified
     *                         as a positive or negative offset value but should be
     *                         between -24 to 24 hours inclusive (expressed in millis)
     *                         constraint: startOffsetMillis < stopOffsetMillis
     * @return a time fence
     */
    @NonNull
    public static StorableTimeFence aroundTimeInstant(@TimeInstant int timeInstant,
                                                      long startOffsetMillis,
                                                      long stopOffsetMillis) {
        return new StorableTimeFence(TIME_INSTANT, DAY_OF_WEEK_SUNDAY, TIME_INTERVAL_WEEKDAY,
                timeInstant, null, 0, 0, startOffsetMillis, stopOffsetMillis);
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
     * @deprecated use {@link StorableTimeFence#inIntervalOfDay(int, TimeZone, long, long)}
     */
    public static StorableTimeFence inMondayInterval(TimeZone timeZone, long startTimeOfDayMillis,
                                                     long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAY_OF_WEEK, DAY_OF_WEEK_MONDAY, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis, 0, 0);
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
     * @deprecated use {@link StorableTimeFence#inIntervalOfDay(int, TimeZone, long, long)}
     */
    public static StorableTimeFence inTuesdayInterval(TimeZone timeZone, long startTimeOfDayMillis,
                                                      long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAY_OF_WEEK, DAY_OF_WEEK_TUESDAY, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis, 0, 0);
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
     * @deprecated use {@link StorableTimeFence#inIntervalOfDay(int, TimeZone, long, long)}
     */
    public static StorableTimeFence inWednesdayInterval(TimeZone timeZone,
                                                        long startTimeOfDayMillis,
                                                        long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAY_OF_WEEK, DAY_OF_WEEK_WEDNESDAY, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis, 0, 0);
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
     * @deprecated use {@link StorableTimeFence#inIntervalOfDay(int, TimeZone, long, long)}
     */
    public static StorableTimeFence inThursdayInterval(TimeZone timeZone, long startTimeOfDayMillis,
                                                       long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAY_OF_WEEK, DAY_OF_WEEK_THURSDAY, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis, 0, 0);
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
     * @deprecated use {@link StorableTimeFence#inIntervalOfDay(int, TimeZone, long, long)}
     */
    public static StorableTimeFence inFridayInterval(TimeZone timeZone, long startTimeOfDayMillis,
                                                     long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAY_OF_WEEK, DAY_OF_WEEK_FRIDAY, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis, 0, 0);
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
     * @deprecated use {@link StorableTimeFence#inIntervalOfDay(int, TimeZone, long, long)}
     */
    public static StorableTimeFence inSaturdayInterval(TimeZone timeZone, long startTimeOfDayMillis,
                                                       long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAY_OF_WEEK, DAY_OF_WEEK_SATURDAY, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis, 0, 0);
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
     * @deprecated use {@link StorableTimeFence#inIntervalOfDay(int, TimeZone, long, long)}
     */
    public static StorableTimeFence inSundayInterval(TimeZone timeZone, long startTimeOfDayMillis,
                                                     long stopTimeOfDayMillis) {
        return new StorableTimeFence(DAY_OF_WEEK, DAY_OF_WEEK_SUNDAY, TIME_INTERVAL_WEEKDAY,
                TIME_INSTANT_SUNRISE, timeZone, startTimeOfDayMillis, stopTimeOfDayMillis, 0, 0);
    }

    static JSONObject timeFenceToString(StorableFence fence, JSONObject json) {
        if (fence.getType() == Type.TIME) {
            StorableTimeFence timeFence = (StorableTimeFence) fence;
            try {
                json.put(FENCE_TYPE_KEY, Type.TIME.ordinal());
                json.put(TIMING_TYPE_KEY, timeFence.getTimingType());
                json.put(DAY_OF_WEEK_KEY, timeFence.getDayOfWeek());
                json.put(TIME_INTERVAL_KEY, timeFence.getTimeInterval());
                json.put(TIME_INSTANT_KEY, timeFence.getTimeInstant());
                TimeZone timeZone = timeFence.getTimeZone();
                if (timeZone != null) {
                    json.put(TIMEZONE_OFFSET_KEY, timeZone.getRawOffset());
                    json.put(TIMEZONE_ID_KEY, timeZone.getID());
                }
                json.put(START_TIME_KEY, timeFence.getStartTime());
                json.put(STOP_TIME_KEY, timeFence.getStopTime());
                json.put(START_OFFSET_KEY, timeFence.getStartOffset());
                json.put(STOP_OFFSET_KEY, timeFence.getStopOffset());
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
                    timeZone = new SimpleTimeZone(jsonObj.getInt(TIMEZONE_OFFSET_KEY),
                            jsonObj.getString(TIMEZONE_ID_KEY));
                }
                long startTime = jsonObj.getLong(START_TIME_KEY);
                long stopTime = jsonObj.getLong(STOP_TIME_KEY);
                switch (timingType) {
                    case ABSOLUTE:
                        return StorableTimeFence.inInterval(startTime, stopTime);
                    case DAILY:
                        return StorableTimeFence.inDailyInterval(timeZone, startTime, stopTime);
                    case DAY_OF_WEEK:
                        @DayOfWeek int dayOfWeek = jsonObj.getInt(DAY_OF_WEEK_KEY);
                        return StorableTimeFence.inIntervalOfDay(dayOfWeek, timeZone, startTime,
                                stopTime);
                    case TIME_INTERVAL:
                        @TimeInterval int timeInterval = jsonObj.getInt(TIME_INTERVAL_KEY);
                        return StorableTimeFence.inTimeInterval(timeInterval);
                    case TIME_INSTANT:
                        @TimeInstant int timeIntant = jsonObj.getInt(TIME_INSTANT_KEY);
                        long startOffset = jsonObj.getLong(START_OFFSET_KEY);
                        long stopOffset = jsonObj.getLong(STOP_OFFSET_KEY);
                        return StorableTimeFence.aroundTimeInstant(timeIntant, startOffset,
                                stopOffset);
                    case MONDAY:
                        return StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_MONDAY, timeZone,
                                startTime, stopTime);
                    case TUESDAY:
                        return StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_TUESDAY, timeZone,
                                startTime, stopTime);
                    case WEDNESDAY:
                        return StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_WEDNESDAY, timeZone,
                                startTime, stopTime);
                    case THURSDAY:
                        return StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_THURSDAY, timeZone,
                                startTime, stopTime);
                    case FRIDAY:
                        return StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_FRIDAY, timeZone,
                                startTime, stopTime);
                    case SATURDAY:
                        return StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_SATURDAY, timeZone,
                                startTime, stopTime);
                    case SUNDAY:
                        return StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_SUNDAY, timeZone,
                                startTime, stopTime);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
