package com.sousoum.jcvd.matchers;

import android.support.annotation.Nullable;

import com.sousoum.jcvd.StorableTimeFence;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.TimeZone;

import static com.sousoum.jcvd.StorableTimeFence.ABSOLUTE;
import static com.sousoum.jcvd.StorableTimeFence.DAILY;
import static com.sousoum.jcvd.StorableTimeFence.DAY_OF_WEEK;
import static com.sousoum.jcvd.StorableTimeFence.TIME_INSTANT;
import static com.sousoum.jcvd.StorableTimeFence.TIME_INTERVAL;
import static org.hamcrest.core.IsEqual.equalTo;

public class StorableTimeFenceMatcher {

    public static Matcher<StorableTimeFence> isAbsolute(long start, long stop) {
        return Matchers.allOf(
                new FeatureMatcher<StorableTimeFence, Integer>(equalTo(ABSOLUTE), "timingType is", "timingType") {
                    @Override
                    protected Integer featureValueOf(StorableTimeFence fence) {
                        return fence.getTimingType();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Long>(equalTo(start), "start time is", "start time") {
                    @Override
                    protected Long featureValueOf(StorableTimeFence fence) {
                        return fence.getStartTime();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Long>(equalTo(stop), "stop time is", "stop time") {
                    @Override
                    protected Long featureValueOf(StorableTimeFence fence) {
                        return fence.getStopTime();
                    }
                }
        );
    }

    public static Matcher<StorableTimeFence> isDaily(@Nullable TimeZone timezone, long start,
                                                     long stop) {
        return Matchers.allOf(
                new FeatureMatcher<StorableTimeFence, Integer>(equalTo(DAILY), "timingType is", "timingType") {
                    @Override
                    protected Integer featureValueOf(StorableTimeFence fence) {
                        return fence.getTimingType();
                    }
                },
                new FeatureMatcher<StorableTimeFence, TimeZone>(equalTo(timezone), "timeZone is", "timeZone") {
                    @Override
                    protected TimeZone featureValueOf(StorableTimeFence fence) {
                        return fence.getTimeZone();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Long>(equalTo(start), "start time is", "start time") {
                    @Override
                    protected Long featureValueOf(StorableTimeFence fence) {
                        return fence.getStartTime();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Long>(equalTo(stop), "stop time is", "stop time") {
                    @Override
                    protected Long featureValueOf(StorableTimeFence fence) {
                        return fence.getStopTime();
                    }
                }
        );
    }

    public static Matcher<StorableTimeFence> isDayOfWeek(@StorableTimeFence.DayOfWeek int dayOfWeek,
                                                         @Nullable TimeZone timezone, long start,
                                                         long stop) {
        return Matchers.allOf(
                new FeatureMatcher<StorableTimeFence, Integer>(equalTo(DAY_OF_WEEK), "timingType is", "timingType") {
                    @Override
                    protected Integer featureValueOf(StorableTimeFence fence) {
                        return fence.getTimingType();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Integer>(equalTo(dayOfWeek), "day of week is", "day of week") {
                    @Override
                    protected Integer featureValueOf(StorableTimeFence fence) {
                        return fence.getDayOfWeek();
                    }
                },
                new FeatureMatcher<StorableTimeFence, TimeZone>(equalTo(timezone), "timeZone is", "timeZone") {
                    @Override
                    protected TimeZone featureValueOf(StorableTimeFence fence) {
                        return fence.getTimeZone();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Long>(equalTo(start), "start time is", "start time") {
                    @Override
                    protected Long featureValueOf(StorableTimeFence fence) {
                        return fence.getStartTime();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Long>(equalTo(stop), "stop time is", "stop time") {
                    @Override
                    protected Long featureValueOf(StorableTimeFence fence) {
                        return fence.getStopTime();
                    }
                }
        );
    }

    public static Matcher<StorableTimeFence> isTimeInterval(@StorableTimeFence.TimeInterval int timeInterval) {
        return Matchers.allOf(
                new FeatureMatcher<StorableTimeFence, Integer>(equalTo(TIME_INTERVAL), "timingType is", "timingType") {
                    @Override
                    protected Integer featureValueOf(StorableTimeFence fence) {
                        return fence.getTimingType();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Integer>(equalTo(timeInterval), "time interval is", "time interval") {
                    @Override
                    protected Integer featureValueOf(StorableTimeFence fence) {
                        return fence.getTimeInterval();
                    }
                }
        );
    }

    public static Matcher<StorableTimeFence> isTimeInstant(@StorableTimeFence.TimeInstant int timeInstant,
                                                           long start, long stop) {
        return Matchers.allOf(
                new FeatureMatcher<StorableTimeFence, Integer>(equalTo(TIME_INSTANT), "timingType is", "timingType") {
                    @Override
                    protected Integer featureValueOf(StorableTimeFence fence) {
                        return fence.getTimingType();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Integer>(equalTo(timeInstant), "time instant is", "time instant") {
                    @Override
                    protected Integer featureValueOf(StorableTimeFence fence) {
                        return fence.getTimeInstant();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Long>(equalTo(start), "start offset is", "start offset") {
                    @Override
                    protected Long featureValueOf(StorableTimeFence fence) {
                        return fence.getStartOffset();
                    }
                },
                new FeatureMatcher<StorableTimeFence, Long>(equalTo(stop), "stop offset is", "stop offset") {
                    @Override
                    protected Long featureValueOf(StorableTimeFence fence) {
                        return fence.getStopOffset();
                    }
                }
        );
    }
}
