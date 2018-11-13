package com.sousoum.jcvd;

import com.google.android.gms.awareness.fence.DetectedActivityFence;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StorableActivityFenceTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testValues() {
        StorableActivityFence fence = StorableActivityFence.starting(
                DetectedActivityFence.IN_VEHICLE, DetectedActivityFence.RUNNING);
        int[] startActivities = {DetectedActivityFence.IN_VEHICLE, DetectedActivityFence.RUNNING};
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.ACTIVITY));
        assertThat(fence.getActivityTypes(), is(startActivities));
        assertThat(fence.getTransitionType(), is(StorableActivityFence.START_TYPE));

        fence = StorableActivityFence.stopping(
                DetectedActivityFence.ON_BICYCLE, DetectedActivityFence.WALKING);
        int[] stopActivities = {DetectedActivityFence.ON_BICYCLE, DetectedActivityFence.WALKING};
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.ACTIVITY));
        assertThat(fence.getActivityTypes(), is(stopActivities));
        assertThat(fence.getTransitionType(), is(StorableActivityFence.STOP_TYPE));

        fence = StorableActivityFence.during(
                DetectedActivityFence.ON_FOOT, DetectedActivityFence.STILL, DetectedActivityFence.UNKNOWN);
        int[] duringActivities = {DetectedActivityFence.ON_FOOT, DetectedActivityFence.STILL, DetectedActivityFence.UNKNOWN};
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.ACTIVITY));
        assertThat(fence.getActivityTypes(), is(duringActivities));
        assertThat(fence.getTransitionType(), is(StorableActivityFence.DURING_TYPE));
    }

    @Test
    public void testEquals() {
        StorableActivityFence fence1 = StorableActivityFence.starting(
                DetectedActivityFence.IN_VEHICLE, DetectedActivityFence.RUNNING);
        StorableActivityFence fence2 = StorableActivityFence.stopping(
                DetectedActivityFence.ON_BICYCLE, DetectedActivityFence.WALKING);
        StorableActivityFence fence3 = StorableActivityFence.stopping(
                DetectedActivityFence.ON_BICYCLE, DetectedActivityFence.WALKING);

        assertThat(fence1.equals(fence1), is(true));
        assertThat(fence2.equals(fence3), is(true));
        assertThat(fence3.equals(null), is(false));
        assertThat(fence3.equals(fence1), is(false));
    }
}