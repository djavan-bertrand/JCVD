package com.sousoum.jcvd.matchers;

import com.sousoum.jcvd.StorableLocationFence;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static org.hamcrest.core.IsEqual.equalTo;

public class StorableLocationFenceMatcher {
    public static Matcher<StorableLocationFence> is(@StorableLocationFence.TransitionType int transition,
                                                    double latitude, double longitude,
                                                    double radius, long dwell) {
        return Matchers.allOf(
                new FeatureMatcher<StorableLocationFence, Integer>(equalTo(transition), "transition is", "transition") {
                    @Override
                    protected Integer featureValueOf(StorableLocationFence fence) {
                        return fence.getTransitionType();
                    }
                },
                new FeatureMatcher<StorableLocationFence, Double>(equalTo(latitude), "latitude is", "latitude") {
                    @Override
                    protected Double featureValueOf(StorableLocationFence fence) {
                        return fence.getLatitude();
                    }
                },
                new FeatureMatcher<StorableLocationFence, Double>(equalTo(longitude), "longitude is", "longitude") {
                    @Override
                    protected Double featureValueOf(StorableLocationFence fence) {
                        return fence.getLongitude();
                    }
                },
                new FeatureMatcher<StorableLocationFence, Double>(equalTo(radius), "radius is", "radius") {
                    @Override
                    protected Double featureValueOf(StorableLocationFence fence) {
                        return fence.getRadius();
                    }
                },
                new FeatureMatcher<StorableLocationFence, Long>(equalTo(dwell), "dwell is", "dwell") {
                    @Override
                    protected Long featureValueOf(StorableLocationFence fence) {
                        return fence.getDwellTimeMillis();
                    }
                }
        );
    }
}
