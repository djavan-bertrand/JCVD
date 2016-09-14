package com.sousoum.jcvd;

import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.state.HeadphoneState;

import junit.framework.TestCase;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.TimeZone;

import static com.sousoum.jcvd.matchers.StorableFenceMatcher.isAnd;
import static com.sousoum.jcvd.matchers.StorableFenceMatcher.isNot;
import static com.sousoum.jcvd.matchers.StorableFenceMatcher.isNotAnd;
import static com.sousoum.jcvd.matchers.StorableFenceMatcher.isNotNot;
import static com.sousoum.jcvd.matchers.StorableFenceMatcher.isNotOr;
import static com.sousoum.jcvd.matchers.StorableFenceMatcher.isOr;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(android.support.test.runner.AndroidJUnit4.class)
public class StorableFenceTest extends TestCase {

    private StorableActivityFence mAct1;

    private StorableActivityFence mAct2;

    private StorableLocationFence mLoc1;

    private StorableLocationFence mLoc2;

    private StorableHeadphoneFence mHead1;

    private StorableTimeFence mTime1;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mAct1 = StorableActivityFence.starting(DetectedActivityFence.IN_VEHICLE);
        mAct1.setPendingIntentClass("className");
        mAct2 = StorableActivityFence.during(DetectedActivityFence.RUNNING);
        mLoc1 = StorableLocationFence.entering(2, 3, 30);
        mLoc1.setId("1");
        mLoc2 = StorableLocationFence.exiting(3, 4, 40);
        mHead1 = StorableHeadphoneFence.during(HeadphoneState.PLUGGED_IN);
        mTime1 = StorableTimeFence.inFridayInterval(TimeZone.getDefault(), 20, 20000);

    }

    @Test
    public void testAnd() {
        // test with two activity fence
        StorableFence andFence = StorableFence.and(mAct1, mAct2);
        assertThat(andFence.getType(), Matchers.is(StorableFence.Type.META));
        assertThat(andFence, allOf(
                notNullValue(),
                isAnd(mAct1, mAct2),
                isNotOr(),
                isNotNot()));

        // test with different fence types
        andFence = StorableFence.and(mAct1, mLoc1, mLoc2);
        assertThat(andFence.getType(), Matchers.is(StorableFence.Type.META));
        assertThat(andFence, allOf(
                notNullValue(),
                isAnd(mAct1, mLoc1, mLoc2),
                isNotOr(),
                isNotNot()));
    }

    @Test
    public void testOr() {
        // test with two location fence
        StorableFence orFence = StorableFence.or(mLoc1, mLoc2);
        assertThat(orFence.getType(), Matchers.is(StorableFence.Type.META));
        assertThat(orFence, allOf(
                notNullValue(),
                isOr(mLoc1, mLoc2),
                isNotAnd(),
                isNotNot()));

        // test with different fence types
        orFence = StorableFence.or(mAct1, mLoc1, mLoc2);
        assertThat(orFence.getType(), Matchers.is(StorableFence.Type.META));
        assertThat(orFence, allOf(
                notNullValue(),
                isOr(mAct1, mLoc1, mLoc2),
                isNotAnd(),
                isNotNot()));
    }

    @Test
    public void testNot() {
        // test with two location fence
        StorableFence notFence = StorableFence.not(mLoc1);
        assertThat(notFence.getType(), Matchers.is(StorableFence.Type.META));
        assertThat(notFence, allOf(
                notNullValue(),
                isNot(mLoc1),
                isNotAnd(),
                isNotOr()));

        // test the negation of a meta
        StorableFence notFence2 = StorableFence.not(notFence);
        assertThat(notFence.getType(), Matchers.is(StorableFence.Type.META));
        assertThat(notFence2, allOf(
                notNullValue(),
                isNot(notFence),
                isNotAnd(),
                isNotOr()));
        // also check that it has not changed to negate the fence
        assertThat(notFence2.getNotFence(), allOf(
                notNullValue(),
                isNot(mLoc1),
                isNotAnd(),
                isNotOr()));
    }

    @Test
    public void testStringConvert() {
        StorableFence orFence = StorableFence.or(mLoc1, mLoc2, mHead1);
        StorableFence notFence = StorableFence.not(mAct1);
        StorableFence andFence = StorableFence.and(orFence, notFence, mAct2, mTime1);
        HashMap<String, Object> additionalData = new HashMap<>();
        additionalData.put("long", Long.MAX_VALUE);
        additionalData.put("int", Integer.MIN_VALUE);
        additionalData.put("string", "4");
        additionalData.put("double", Double.MAX_VALUE);
        additionalData.put("boolean", true);
        andFence.setAdditionalData(additionalData);

        String str = StorableFence.fenceToString(andFence);

        StorableFence retrievedFence = StorableFence.stringToFence(str);
        String str2 = StorableFence.fenceToString(retrievedFence);
        assertThat(str, is(str2));
        assertThat(andFence, is(retrievedFence));
    }

    @Test
    public void testSetters() {
        mLoc2.setId("2");
        assertThat(mLoc2.getId(), is("2"));

        mLoc2.setPendingIntentClass("class");
        assertThat(mLoc2.getPendingIntentClass(), is("class"));
    }

    @Test
    public void testEquals() {
        StorableFence orFence = StorableFence.or(mLoc1, mLoc2);
        StorableFence notFence = StorableFence.not(mAct1);
        StorableFence andFence = StorableFence.and(orFence, notFence, mAct2);

        StorableFence orFence2 = StorableFence.or(mLoc1, mLoc2);
        StorableFence notFence2 =
                StorableFence.not(mAct1);
        StorableFence andFence2 = StorableFence.and(orFence2, notFence2, mAct2);

        assertThat(andFence.equals(andFence), is(true));
        assertThat(andFence.equals(andFence2), is(true));
        assertThat(andFence.equals(null), is(false));
        assertThat(andFence.equals(orFence2), is(false));
    }
}