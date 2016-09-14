package com.sousoum.jcvd;

import com.google.android.gms.awareness.state.HeadphoneState;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(android.support.test.runner.AndroidJUnit4.class)
public class StorableHeadphoneFenceTest extends TestCase {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

    }

    @Test
    public void testValues() {
        StorableHeadphoneFence fence = StorableHeadphoneFence.during(HeadphoneState.PLUGGED_IN);
        assertThat(fence.getType(), is(StorableFence.Type.HEADPHONE));
        assertThat(fence.getTriggerType(), is(StorableHeadphoneFence.STATE));
        assertThat(fence.getHeadphoneState(), is(HeadphoneState.PLUGGED_IN));

        fence = StorableHeadphoneFence.pluggingIn();
        assertThat(fence.getType(), is(StorableFence.Type.HEADPHONE));
        assertThat(fence.getTriggerType(), is(StorableHeadphoneFence.PLUGGING_IN));

        fence = StorableHeadphoneFence.unplugging();
        assertThat(fence.getType(), is(StorableFence.Type.HEADPHONE));
        assertThat(fence.getTriggerType(), is(StorableHeadphoneFence.UNPLUGGING));
    }

    @Test
    public void testEquals() {
        StorableHeadphoneFence fence1 = StorableHeadphoneFence.during(HeadphoneState.PLUGGED_IN);
        StorableHeadphoneFence fence2 = StorableHeadphoneFence.during(HeadphoneState.UNPLUGGED);
        StorableHeadphoneFence fence3 = StorableHeadphoneFence.pluggingIn();
        StorableHeadphoneFence fence4 = StorableHeadphoneFence.unplugging();
        StorableHeadphoneFence fence5 = StorableHeadphoneFence.unplugging();

        assertThat(fence1.equals(fence1), is(true));
        assertThat(fence1.equals(fence2), is(false));
        assertThat(fence2.equals(fence3), is(false));
        assertThat(fence3.equals(fence4), is(false));
        assertThat(fence3.equals(null), is(false));
        assertThat(fence4.equals(fence5), is(true));
    }
}