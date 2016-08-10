package com.sousoum.jcvd.matchers;

import com.sousoum.jcvd.StorableFence;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static org.hamcrest.core.IsEqual.equalTo;

public class StorableFenceMatcher {
    public static Matcher<StorableFence> isAnd(StorableFence... fences) {
        return Matchers.allOf(
                new FeatureMatcher<StorableFence, Boolean>(equalTo(true), "andFences not empty", "andFences not empty") {
                    @Override
                    protected Boolean featureValueOf(StorableFence fence) {
                        return !fence.getAndFences().isEmpty();
                    }
                },
                new FeatureMatcher<StorableFence, StorableFence[]>(equalTo(fences), "has same andFences", "has same andFences") {
                    @Override
                    protected StorableFence[] featureValueOf(StorableFence fence) {
                        return fence.getAndFences().toArray(new StorableFence[fence.getAndFences().size()]);
                    }
                }
        );
    }

    public static Matcher<StorableFence> isNotAnd() {
        return new FeatureMatcher<StorableFence, Boolean>(equalTo(true), "andFences empty", "andFences empty") {
                    @Override
                    protected Boolean featureValueOf(StorableFence fence) {
                        return fence.getAndFences().isEmpty();
                    }
                };
    }

    public static Matcher<StorableFence> isOr(StorableFence... fences) {
        return Matchers.allOf(
                new FeatureMatcher<StorableFence, Boolean>(equalTo(true), "orFences not empty", "orFences not empty") {
                    @Override
                    protected Boolean featureValueOf(StorableFence fence) {
                        return !fence.getOrFences().isEmpty();
                    }
                },
                new FeatureMatcher<StorableFence, StorableFence[]>(equalTo(fences), "has same orFences", "has same orFences") {
                    @Override
                    protected StorableFence[] featureValueOf(StorableFence fence) {
                        return fence.getOrFences().toArray(new StorableFence[fence.getOrFences().size()]);
                    }
                }
        );
    }

    public static Matcher<StorableFence> isNotOr() {
        return new FeatureMatcher<StorableFence, Boolean>(equalTo(true), "orFences empty", "orFences empty") {
            @Override
            protected Boolean featureValueOf(StorableFence fence) {
                return fence.getOrFences().isEmpty();
            }
        };
    }

    public static Matcher<StorableFence> isNot(StorableFence fence) {
        return Matchers.allOf(
                new FeatureMatcher<StorableFence, Boolean>(equalTo(true), "notFence not null", "notFence not null") {
                    @Override
                    protected Boolean featureValueOf(StorableFence fence) {
                        return fence.getNotFence() != null;
                    }
                },
                new FeatureMatcher<StorableFence, StorableFence>(equalTo(fence), "has same notFence", "has same notFence") {
                    @Override
                    protected StorableFence featureValueOf(StorableFence fence) {
                        return fence.getNotFence();
                    }
                }
        );
    }

    public static Matcher<StorableFence> isNotNot() {
        return new FeatureMatcher<StorableFence, Boolean>(equalTo(true), "notFence is null", "notFence is null") {
            @Override
            protected Boolean featureValueOf(StorableFence fence) {
                return fence.getNotFence() == null;
            }
        };
    }
}
