package com.sousoum.jcvd;

import com.google.android.gms.awareness.fence.TimeFence;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

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
import static com.google.android.gms.awareness.fence.TimeFence.TIME_INTERVAL_AFTERNOON;
import static com.sousoum.jcvd.matchers.StorableTimeFenceMatcher.isAbsolute;
import static com.sousoum.jcvd.matchers.StorableTimeFenceMatcher.isDaily;
import static com.sousoum.jcvd.matchers.StorableTimeFenceMatcher.isDayOfWeek;
import static com.sousoum.jcvd.matchers.StorableTimeFenceMatcher.isTimeInstant;
import static com.sousoum.jcvd.matchers.StorableTimeFenceMatcher.isTimeInterval;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StorableTimeFenceTest {

    private TimeZone mTimeZone;

    @Before
    public void setUp() {
        mTimeZone = TimeZone.getDefault();
    }

    @Test
    public void testValues() {
        StorableTimeFence fence = StorableTimeFence.inDailyInterval(mTimeZone, 20, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDaily(mTimeZone, 20, 200));

        fence = StorableTimeFence.inInterval(20, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isAbsolute(20, 200));

        fence = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_MONDAY, null, 20, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_MONDAY, null, 20, 200));

        fence = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_TUESDAY, mTimeZone, 20, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_TUESDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_WEDNESDAY, mTimeZone, 20, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_WEDNESDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_THURSDAY, mTimeZone, 20, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_THURSDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_FRIDAY, mTimeZone, 20, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_FRIDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_SATURDAY, mTimeZone, 20, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_SATURDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_SUNDAY, mTimeZone, 20, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_SUNDAY, mTimeZone, 20, 200));

        fence = StorableTimeFence.inTimeInterval(TIME_INTERVAL_AFTERNOON);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isTimeInterval(TIME_INTERVAL_AFTERNOON));

        fence = StorableTimeFence.aroundTimeInstant(TIME_INSTANT_SUNRISE, -200, 200);
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isTimeInstant(TIME_INSTANT_SUNRISE, -200, 200));
    }

    @Test
    public void testDeprecated() {
        JSONObject root = new JSONObject();
        StorableTimeFence fence = StorableTimeFence.inMondayInterval(null, 20, 200);
        StorableTimeFence copiedFence = (StorableTimeFence) StorableTimeFence.jsonToTimeFence(
                StorableTimeFence.timeFenceToString(fence, root));
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_MONDAY, null, 20, 200));
        assertThat(fence, is(copiedFence));

        fence = StorableTimeFence.inTuesdayInterval(mTimeZone, 20, 200);
        copiedFence = (StorableTimeFence) StorableTimeFence.jsonToTimeFence(
                StorableTimeFence.timeFenceToString(fence, root));
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_TUESDAY, mTimeZone, 20, 200));
        assertThat(fence, is(copiedFence));

        fence = StorableTimeFence.inWednesdayInterval(mTimeZone, 20, 200);
        copiedFence = (StorableTimeFence) StorableTimeFence.jsonToTimeFence(
                StorableTimeFence.timeFenceToString(fence, root));
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_WEDNESDAY, mTimeZone, 20, 200));
        assertThat(fence, is(copiedFence));

        fence = StorableTimeFence.inThursdayInterval(mTimeZone, 20, 200);
        copiedFence = (StorableTimeFence) StorableTimeFence.jsonToTimeFence(
                StorableTimeFence.timeFenceToString(fence, root));
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_THURSDAY, mTimeZone, 20, 200));
        assertThat(fence, is(copiedFence));

        fence = StorableTimeFence.inFridayInterval(mTimeZone, 20, 200);
        copiedFence = (StorableTimeFence) StorableTimeFence.jsonToTimeFence(
                StorableTimeFence.timeFenceToString(fence, root));
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_FRIDAY, mTimeZone, 20, 200));
        assertThat(fence, is(copiedFence));

        fence = StorableTimeFence.inSaturdayInterval(mTimeZone, 20, 200);
        copiedFence = (StorableTimeFence) StorableTimeFence.jsonToTimeFence(
                StorableTimeFence.timeFenceToString(fence, root));
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_SATURDAY, mTimeZone, 20, 200));
        assertThat(fence, is(copiedFence));

        fence = StorableTimeFence.inSundayInterval(mTimeZone, 20, 200);
        copiedFence = (StorableTimeFence) StorableTimeFence.jsonToTimeFence(
                StorableTimeFence.timeFenceToString(fence, root));
        assertThat(fence.getType(), is(StorableFence.Type.TIME));
        assertThat(fence, isDayOfWeek(DAY_OF_WEEK_SUNDAY, mTimeZone, 20, 200));
        assertThat(fence, is(copiedFence));

    }

    @Test
    public void testEquals() {
        StorableTimeFence fence1 = StorableTimeFence.inInterval(2, 300);
        StorableTimeFence fence2 = StorableTimeFence.inInterval(2, 400);
        StorableTimeFence fence3 = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_MONDAY, mTimeZone, 20, 300);
        StorableTimeFence fence4 = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_MONDAY, mTimeZone, 20, 300);
        StorableTimeFence fence5 = StorableTimeFence.inIntervalOfDay(DAY_OF_WEEK_MONDAY, new SimpleTimeZone(3, "1"), 20, 400);
        StorableTimeFence fence6 = StorableTimeFence.aroundTimeInstant(TimeFence.TIME_INSTANT_SUNRISE, 0, 1);
        StorableTimeFence fence7 = StorableTimeFence.aroundTimeInstant(TimeFence.TIME_INSTANT_SUNRISE, 0, 1);
        StorableTimeFence fence8 = StorableTimeFence.inTimeInterval(TIME_INTERVAL_AFTERNOON);
        StorableTimeFence fence9 = StorableTimeFence.inTimeInterval(TimeFence.TIME_INTERVAL_WEEKDAY);

        assertThat(fence1.equals(fence1), is(true));
        assertThat(fence3.equals(fence4), is(true));
        assertThat(fence2.equals(null), is(false));
        assertThat(fence4.equals(fence5), is(false));
        assertThat(fence5.equals(fence6), is(false));
        assertThat(fence6.equals(fence7), is(true));
        assertThat(fence8.equals(fence9), is(false));
    }
}