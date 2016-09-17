package com.sousoum.jcvd.matchers;

import com.sousoum.jcvd.StorableTimeFence;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.TimeZone;

import static org.hamcrest.core.IsEqual.equalTo;

public class StorableTimeFenceMatcher {
    public static Matcher<StorableTimeFence> is(@StorableTimeFence.TimingType int timingType,
                                                    TimeZone tz, long start, long stop) {
        return Matchers.allOf(
                new FeatureMatcher<StorableTimeFence, Integer>(equalTo(timingType), "timingType is", "timingType") {
                    @Override
                    protected Integer featureValueOf(StorableTimeFence fence) {
                        return fence.getTimingType();
                    }
                },
                new FeatureMatcher<StorableTimeFence, TimeZone>(equalTo(tz), "timeZone is", "timeZone") {
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
}
