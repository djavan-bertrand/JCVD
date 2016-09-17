package com.sousoum.jcvd;

import junit.framework.TestCase;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static com.sousoum.jcvd.matchers.StorableTimeFenceMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(android.support.test.runner.AndroidJUnit4.class)
public class StorableTimeFenceTest extends TestCase {

    private TimeZone mTimeZone;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mTimeZone = TimeZone.getDefault();
    }

    @Test
    public void testValues() {
        StorableTimeFence fence = StorableTimeFence.inDailyInterval(mTimeZone, 20, 200);
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.TIME));
        assertThat(fence, is(StorableTimeFence.DAILY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inInterval(20, 200);
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.TIME));
        assertThat(fence, is(StorableTimeFence.ABSOLUTE, null, 20, 200));

        fence = StorableTimeFence.inMondayInterval(mTimeZone, 20, 200);
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.TIME));
        assertThat(fence, is(StorableTimeFence.MONDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inTuesdayInterval(mTimeZone, 20, 200);
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.TIME));
        assertThat(fence, is(StorableTimeFence.TUESDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inWednesdayInterval(mTimeZone, 20, 200);
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.TIME));
        assertThat(fence, is(StorableTimeFence.WEDNESDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inThursdayInterval(mTimeZone, 20, 200);
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.TIME));
        assertThat(fence, is(StorableTimeFence.THURSDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inFridayInterval(mTimeZone, 20, 200);
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.TIME));
        assertThat(fence, is(StorableTimeFence.FRIDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inSaturdayInterval(mTimeZone, 20, 200);
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.TIME));
        assertThat(fence, is(StorableTimeFence.SATURDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inSundayInterval(mTimeZone, 20, 200);
        assertThat(fence.getType(), Matchers.is(StorableFence.Type.TIME));
        assertThat(fence, is(StorableTimeFence.SUNDAY, mTimeZone, 20, 200));
    }

    @Test
    public void testEquals() {
        StorableTimeFence fence1 = StorableTimeFence.inInterval(2, 300);
        StorableTimeFence fence2 = StorableTimeFence.inInterval(2, 400);
        StorableTimeFence fence3 = StorableTimeFence.inMondayInterval(mTimeZone, 20, 300);
        StorableTimeFence fence4 = StorableTimeFence.inMondayInterval(mTimeZone, 20, 300);
        StorableTimeFence fence5 = StorableTimeFence.inMondayInterval(new SimpleTimeZone(3, "1"), 20, 400);

        assertThat(fence1.equals(fence1), Matchers.is(true));
        assertThat(fence3.equals(fence4), Matchers.is(true));
        assertThat(fence2.equals(null), Matchers.is(false));
        assertThat(fence4.equals(fence5), Matchers.is(false));
    }
}