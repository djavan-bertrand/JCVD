package com.sousoum.jcvd;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.mock.MockContext;

import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.sousoum.jcvd.mocks.MockSharedPreferences;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(android.support.test.runner.AndroidJUnit4.class)
public class FenceStoreTest extends TestCase {

    private FenceStore mStore;

    private StorableActivityFence mAct1;
    private StorableActivityFence mAct2;
    private StorableLocationFence mLoc1;
    private StorableLocationFence mLoc2;
    private StorableFence mAnd;
    private StorableFence mOr;
    private StorableFence mNot;


    @Before
    public void setUp() throws Exception {
        mStore = new FenceStore(mContext, "test");
        mAct1 = StorableActivityFence.starting(DetectedActivityFence.IN_VEHICLE);
        mAct2 = StorableActivityFence.during(DetectedActivityFence.RUNNING);
        mLoc1 = StorableLocationFence.entering(2, 3, 30);
        mLoc2 = StorableLocationFence.exiting(3, 4, 40);
        mAnd = StorableFence.and(mAct1, mAct2);
        mNot = StorableFence.or(mLoc1);
        mOr = StorableFence.or(mNot, mAnd, mLoc2);
    }

    @Test
    public void testDefault() {
        mStore = new FenceStore(mContext, "test");
        assertThat(mStore.getAllFenceIds(), empty());
        assertThat(mStore.getAllFences(), empty());
    }

    @Test
    public void testStoreAndRemoveFences() {
        Set<String> expectedIds = new HashSet<>();
        List<StorableFence> expectedFences = new ArrayList<>();
        mStore = new FenceStore(mContext, "test");
        assertThat(mPref.getChangeCnt(), is(0));

        // check that removing a non existing fence does nothing
        mStore.removeFence("nop");
        assertThat(mStore.getAllFenceIds(), empty());
        assertThat(mStore.getAllFences(), empty());

        mAct1.setId("act1");
        mStore.storeFence(mAct1);

        expectedIds.add("act1");
        expectedFences.add(mAct1);
        assertThat(mPref.getChangeCnt(), is(2));
        assertThat(mStore.getAllFenceIds(), is(expectedIds));
        assertThat(mStore.getAllFences(), is(expectedFences));

        mOr.setId("or");
        mStore.storeFence(mOr);

        expectedIds.add("or");
        expectedFences.add(mOr);
        assertThat(mPref.getChangeCnt(), is(4));
        assertThat(mStore.getAllFenceIds(), is(expectedIds));
        assertThat(mStore.getAllFences(), is(expectedFences));

        // test that adding the same fence does not change anything
        mStore.storeFence(mOr);
        assertThat(mPref.getChangeCnt(), is(6));
        assertThat(mStore.getAllFenceIds(), is(expectedIds));
        assertThat(mStore.getAllFences(), is(expectedFences));

        // test remove
        mStore.removeFence("act1");
        expectedIds.remove("act1");
        expectedFences.remove(mAct1);
        assertThat(mPref.getChangeCnt(), is(7));
        assertThat(mStore.getAllFenceIds(), is(expectedIds));
        assertThat(mStore.getAllFences(), is(expectedFences));

        // rest removing a non existing fence
        mStore.removeFence("nop");
        assertThat(mPref.getChangeCnt(), is(7));
        assertThat(mStore.getAllFenceIds(), is(expectedIds));
        assertThat(mStore.getAllFences(), is(expectedFences));
    }

    @Test
    public void testStoreAndRemoveIds() {
        Set<String> expectedIds = new HashSet<>();
        mStore = new FenceStore(mContext, "test");
        assertThat(mPref.getChangeCnt(), is(0));

        mStore.storeFenceId("act1");

        expectedIds.add("act1");
        assertThat(mPref.getChangeCnt(), is(1));
        assertThat(mStore.getAllFenceIds(), is(expectedIds));

        // test that adding the same fence does not change anything
        mStore.storeFenceId("act1");
        assertThat(mPref.getChangeCnt(), is(2));
        assertThat(mStore.getAllFenceIds(), is(expectedIds));

        // test remove
        mStore.removeFence("act1");
        expectedIds.remove("act1");
        assertThat(mPref.getChangeCnt(), is(3));
        assertThat(mStore.getAllFenceIds(), is(expectedIds));

        // rest removing a non existing fence
        mStore.removeFence("nop");
        assertThat(mPref.getChangeCnt(), is(3));
        assertThat(mStore.getAllFenceIds(), is(expectedIds));
    }


    private final MockSharedPreferences mPref = new MockSharedPreferences();

    private final Context mContext = new MockContext() {
        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return mPref;
        }

        @Override
        public Context getApplicationContext() {
            return this;
        }
    };
}