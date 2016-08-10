package com.sousoum.jcvd;

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
                StorableActivityFence.IN_VEHICLE, StorableActivityFence.RUNNING);
        int[] startActivities = {StorableActivityFence.IN_VEHICLE, StorableActivityFence.RUNNING};
        assertThat(fence.getActivityTypes(), is(startActivities));
        assertThat(fence.getTransitionType(), is(StorableActivityFence.START_TYPE));

        fence = StorableActivityFence.stopping(
                StorableActivityFence.ON_BICYCLE, StorableActivityFence.WALKING);
        int[] stopActivities = {StorableActivityFence.ON_BICYCLE, StorableActivityFence.WALKING};
        assertThat(fence.getActivityTypes(), is(stopActivities));
        assertThat(fence.getTransitionType(), is(StorableActivityFence.STOP_TYPE));

        fence = StorableActivityFence.during(
                StorableActivityFence.ON_FOOT, StorableActivityFence.STILL, StorableActivityFence.UNKNOWN);
        int[] duringActivities = {StorableActivityFence.ON_FOOT, StorableActivityFence.STILL, StorableActivityFence.UNKNOWN};
        assertThat(fence.getActivityTypes(), is(duringActivities));
        assertThat(fence.getTransitionType(), is(StorableActivityFence.DURING_TYPE));
    }

    @Test
    public void testEquals() {
        StorableActivityFence fence1 = StorableActivityFence.starting(
                StorableActivityFence.IN_VEHICLE, StorableActivityFence.RUNNING);
        StorableActivityFence fence2 = StorableActivityFence.stopping(
                StorableActivityFence.ON_BICYCLE, StorableActivityFence.WALKING);
        StorableActivityFence fence3 = StorableActivityFence.stopping(
                StorableActivityFence.ON_BICYCLE, StorableActivityFence.WALKING);

        assertThat(fence1.equals(fence1), is(true));
        assertThat(fence2.equals(fence3), is(true));
        assertThat(fence3.equals(null), is(false));
        assertThat(fence3.equals(fence1), is(false));
    }
}