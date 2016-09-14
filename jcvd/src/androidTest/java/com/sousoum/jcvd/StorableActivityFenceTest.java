package com.sousoum.jcvd;

import com.google.android.gms.awareness.fence.DetectedActivityFence;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(android.support.test.runner.AndroidJUnit4.class)
public class StorableActivityFenceTest extends TestCase {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

    }

    @Test
    public void testValues() {
        StorableActivityFence fence = StorableActivityFence.starting(
                DetectedActivityFence.IN_VEHICLE, DetectedActivityFence.RUNNING);
        int[] startActivities = {DetectedActivityFence.IN_VEHICLE, DetectedActivityFence.RUNNING};
        assertThat(fence.getActivityTypes(), is(startActivities));
        assertThat(fence.getTransitionType(), is(StorableActivityFence.START_TYPE));

        fence = StorableActivityFence.stopping(
                DetectedActivityFence.ON_BICYCLE, DetectedActivityFence.WALKING);
        int[] stopActivities = {DetectedActivityFence.ON_BICYCLE, DetectedActivityFence.WALKING};
        assertThat(fence.getActivityTypes(), is(stopActivities));
        assertThat(fence.getTransitionType(), is(StorableActivityFence.STOP_TYPE));

        fence = StorableActivityFence.during(
                DetectedActivityFence.ON_FOOT, DetectedActivityFence.STILL, DetectedActivityFence.UNKNOWN);
        int[] duringActivities = {DetectedActivityFence.ON_FOOT, DetectedActivityFence.STILL, DetectedActivityFence.UNKNOWN};
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