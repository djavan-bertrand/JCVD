package com.sousoum.jcvd;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.test.mock.MockContext;

import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.sousoum.jcvd.mocks.MockSharedPreferences;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(android.support.test.runner.AndroidJUnit4.class)
public class StorableFenceManagerTest extends TestCase {

    private StorableFenceManager mManager;
    private MockGapiFenceManager mMockGapiFenceManager;
    private int mAddedCalls;
    private int mRemovedCalls;

    @Before
    public void setUp() throws Exception {
        mMockGapiFenceManager = new MockGapiFenceManager(mContext);
        MockStorableFenceManager.sMockGapiFenceManager = mMockGapiFenceManager;
        mManager = new MockStorableFenceManager(mContext);
        mManager.setListener(new StorableFenceManager.Listener() {
            @Override
            public void fenceAddStatus(StorableFence fence, Status status) {
                mAddedCalls++;
            }

            @Override
            public void fenceRemoveStatus(String fenceId, Status status) {
                mRemovedCalls++;
            }
        });
    }

    @Test
    public void testDefault() {
        assertThat( mManager.getAllFences(), empty());
    }

    @Test
    public void testAddSucceedWhenNoConnected() {
        mMockGapiFenceManager.isConnected = false;
        StorableFence fence = StorableHeadphoneFence.pluggingIn();
        mManager.addFence("fenceId", fence, "");

        // right after the call, the only thing that should change is that the fence should be added to the toAddStore
        assertThat(mAddedCalls, is(0));
        assertThat(mRemovedCalls, is(0));
        assertThat(mMockGapiFenceManager.addResultDict.containsKey("fenceId"), is(false));
        assertThat(mManager.mToAddStore.getAllFences().size(), is(1));
        assertThat(mManager.mToAddStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mSyncedStore.getAllFences(), empty());
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(nullValue()));

        // when the gapi is connected, the fence should be added to the gapi
        // nothing in the stores should change
        mMockGapiFenceManager.setIsConnected();
        assertThat(mAddedCalls, is(0));
        assertThat(mRemovedCalls, is(0));
        assertThat(mMockGapiFenceManager.addResultDict.containsKey("fenceId"), is(true));
        assertThat(mManager.mToAddStore.getAllFences().size(), is(1));
        assertThat(mManager.mToAddStore.getAllFences().get(0), is(fence));
        assertThat(mManager.mSyncedStore.getAllFences(), empty());
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(nullValue()));

        // when the fence is really added to the gapi, the fence should be placed in the syncedStore
        // and removed from the toAddStore
        // and the listener should also be called
        ResultCallback<Status> result = mMockGapiFenceManager.addResultDict.get("fenceId");
        result.onResult(new Status(CommonStatusCodes.SUCCESS));
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0), is(fence));
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(fence));
    }

    @Test
    public void testAddSucceedWhenConnected() {
        mMockGapiFenceManager.isConnected = true;
        StorableFence fence = StorableHeadphoneFence.during(HeadphoneState.PLUGGED_IN);
        mManager.addFence("fenceId", fence, "");

        // when the gapi is connected, the fence should be added to the gapi
        // nothing in the stores should change
        assertThat(mAddedCalls, is(0));
        assertThat(mRemovedCalls, is(0));
        assertThat(mMockGapiFenceManager.addResultDict.containsKey("fenceId"), is(true));
        assertThat(mManager.mToAddStore.getAllFences().size(), is(1));
        assertThat(mManager.mToAddStore.getAllFences().get(0), is(fence));
        assertThat(mManager.mSyncedStore.getAllFences(), empty());
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(nullValue()));

        // when the fence is really added to the gapi, the fence should be placed in the syncedStore
        // and removed from the toAddStore
        // and the listener should also be called
        ResultCallback<Status> result = mMockGapiFenceManager.addResultDict.get("fenceId");
        result.onResult(new Status(CommonStatusCodes.SUCCESS));
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(fence));
    }

    @Test
    public void testAddFailsWhenConnected() {
        mMockGapiFenceManager.isConnected = true;
        StorableFence fence = StorableHeadphoneFence.unplugging();
        mManager.addFence("fenceId", fence, "");

        // when the gapi is connected, the fence should be added to the gapi
        // nothing in the stores should change
        assertThat(mAddedCalls, is(0));
        assertThat(mRemovedCalls, is(0));
        assertThat(mMockGapiFenceManager.addResultDict.containsKey("fenceId"), is(true));
        assertThat(mManager.mToAddStore.getAllFences().size(), is(1));
        assertThat(mManager.mToAddStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mSyncedStore.getAllFences(), empty());
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(nullValue()));

        // when the fence is really added to the gapi, the fence should be placed in the syncedStore
        // and removed from the toAddStore
        // and the listener should also be called
        ResultCallback<Status> result = mMockGapiFenceManager.addResultDict.get("fenceId");
        result.onResult(new Status(CommonStatusCodes.ERROR, "error message"));
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mManager.mToAddStore.getAllFences().size(), is(1));
        assertThat(mManager.mToAddStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mSyncedStore.getAllFences(), empty());
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(nullValue()));
    }

    @Test
    public void testRemoveSucceedWhenNoConnected() {
        // start with an already added fence
        mMockGapiFenceManager.isConnected = true;
        StorableFence fence = StorableHeadphoneFence.pluggingIn();
        mManager.addFence("fenceId", fence, "");
        ResultCallback<Status> addResult = mMockGapiFenceManager.addResultDict.get("fenceId");
        addResult.onResult(new Status(CommonStatusCodes.SUCCESS));
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(fence));

        mMockGapiFenceManager.isConnected = false;
        mManager.removeFence("fenceId");

        // right after the call, the only thing that should change is that the fence should be added to the toRemoveStore
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mMockGapiFenceManager.removeResultDict.containsKey("fenceId"), is(false));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().size(), is(1));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().contains("fenceId"), is(true));
        assertThat(mManager.getFence("fenceId"), is(fence));

        // when the gapi is connected, the fence should be removed from the gapi
        // nothing in the stores should change
        mMockGapiFenceManager.setIsConnected();
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mMockGapiFenceManager.removeResultDict.containsKey("fenceId"), is(true));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().size(), is(1));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().contains("fenceId"), is(true));
        assertThat(mManager.getFence("fenceId"), is(fence));

        // when the fence is really removed to the gapi, the fence should be removed from the syncedStore
        // and removed from the toRemoveStore
        // and the listener should also be called
        ResultCallback<Status> result = mMockGapiFenceManager.removeResultDict.get("fenceId");
        result.onResult(new Status(CommonStatusCodes.SUCCESS));
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(1));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences(), empty());
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(nullValue()));
    }

    @Test
    public void testRemoveSucceedWhenConnected() {
        // start with an already added fence
        mMockGapiFenceManager.isConnected = true;
        StorableFence fence = StorableTimeFence.inDailyInterval(null, 0, 1);
        mManager.addFence("fenceId", fence, "");
        ResultCallback<Status> addResult = mMockGapiFenceManager.addResultDict.get("fenceId");
        addResult.onResult(new Status(CommonStatusCodes.SUCCESS));
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(fence));

        mManager.removeFence("fenceId");

        // when the gapi is connected, the fence should be removed from the gapi
        // nothing in the stores should change
        mMockGapiFenceManager.setIsConnected();
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mMockGapiFenceManager.removeResultDict.containsKey("fenceId"), is(true));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().size(), is(1));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().contains("fenceId"), is(true));
        assertThat(mManager.getFence("fenceId"), is(fence));

        // when the fence is really removed to the gapi, the fence should be removed from the syncedStore
        // and removed from the toRemoveStore
        // and the listener should also be called
        ResultCallback<Status> result = mMockGapiFenceManager.removeResultDict.get("fenceId");
        result.onResult(new Status(CommonStatusCodes.SUCCESS));
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(1));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences(), empty());
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(nullValue()));
    }

    @Test
    public void testRemoveFailsWhenConnected() {
        // start with an already added fence
        mMockGapiFenceManager.isConnected = true;
        StorableFence fence = StorableTimeFence.inIntervalOfDay(TimeFence.DAY_OF_WEEK_SUNDAY, null, 0, 1);
        mManager.addFence("fenceId", fence, "");
        ResultCallback<Status> addResult = mMockGapiFenceManager.addResultDict.get("fenceId");
        addResult.onResult(new Status(CommonStatusCodes.SUCCESS));
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());
        assertThat(mManager.getFence("fenceId"), is(fence));

        mManager.removeFence("fenceId");

        // when the gapi is connected, the fence should be removed from the gapi
        // nothing in the stores should change
        mMockGapiFenceManager.setIsConnected();
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(0));
        assertThat(mMockGapiFenceManager.removeResultDict.containsKey("fenceId"), is(true));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().size(), is(1));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().contains("fenceId"), is(true));
        assertThat(mManager.getFence("fenceId"), is(fence));

        ResultCallback<Status> result = mMockGapiFenceManager.removeResultDict.get("fenceId");
        result.onResult(new Status(CommonStatusCodes.ERROR, "error message"));
        assertThat(mAddedCalls, is(1));
        assertThat(mRemovedCalls, is(1));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(1));
        assertThat(mManager.mSyncedStore.getAllFences().get(0).equals(fence), is(true));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().size(), is(1));
        assertThat(mManager.mToRemoveStore.getAllFenceIds().contains("fenceId"), is(true));
        assertThat(mManager.getFence("fenceId"), is(fence));
    }

    @Test
    public void testSynchronizeAll() {
        // start with multiple already added fences
        mMockGapiFenceManager.isConnected = true;
        StorableFence fence1 = StorableTimeFence.aroundTimeInstant(TimeFence.TIME_INSTANT_SUNRISE, 0, 1);
        mManager.addFence("fenceId1", fence1, "");
        ResultCallback<Status> addResult = mMockGapiFenceManager.addResultDict.get("fenceId1");
        addResult.onResult(new Status(CommonStatusCodes.SUCCESS));
        StorableFence fence2 = StorableTimeFence.inDailyInterval(null, 0, 1);
        mManager.addFence("fenceId2", fence2, "");
        addResult = mMockGapiFenceManager.addResultDict.get("fenceId2");
        addResult.onResult(new Status(CommonStatusCodes.SUCCESS));

        assertThat(mAddedCalls, is(2));
        assertThat(mRemovedCalls, is(0));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences().size(), is(2));
        assertThat(mManager.mToRemoveStore.getAllFences(), empty());

        // disconnect to add non-committed fences
        mMockGapiFenceManager.isConnected = false;
        mManager.removeFence("fenceId1");
        StorableFence fence3 = StorableTimeFence.inInterval(0, 1);
        mManager.addFence("fenceId3", fence3, "");

        // ask to synchronize all
        mManager.synchronizeAllToGoogleApi();

        // when the gapi is connected:
        // 1: existing fences should have been re-submitted (without statuses)
        // 2: perform non-committed actions
        mMockGapiFenceManager.setIsConnected();
        assertThat(mAddedCalls, is(2));
        assertThat(mRemovedCalls, is(0));
        // check that, even if the status are not set, calls to add existing fences is correctly
        // made
        assertThat(mMockGapiFenceManager.addResultDict, allOf(
                hasEntry("fenceId1", null),
                hasEntry("fenceId2", null)));
        // check that non-committed remove and add have been called
        assertThat(mMockGapiFenceManager.removeResultDict, hasKey("fenceId1"));
        assertThat(mMockGapiFenceManager.addResultDict, hasKey("fenceId3"));
        assertThat(mManager.mToAddStore.getAllFences(), hasSize(1));
        assertThat(mManager.mToAddStore.getAllFences(), contains(fence3));

        assertThat(mManager.mSyncedStore.getAllFences(), hasSize(2));
        assertThat(mManager.mSyncedStore.getAllFences(), containsInAnyOrder(fence1, fence2));

        assertThat(mManager.mToRemoveStore.getAllFenceIds(), hasSize(1));
        assertThat(mManager.mToRemoveStore.getAllFenceIds(), contains("fenceId1"));
        assertThat(mManager.getFence("fenceId1"), is(fence1));
        assertThat(mManager.getFence("fenceId2"), is(fence2));
        assertThat(mManager.getFence("fenceId3"), nullValue());

        ResultCallback<Status> result = mMockGapiFenceManager.removeResultDict.get("fenceId1");
        result.onResult(new Status(CommonStatusCodes.SUCCESS));
        result = mMockGapiFenceManager.addResultDict.get("fenceId3");
        result.onResult(new Status(CommonStatusCodes.SUCCESS));
        assertThat(mAddedCalls, is(3));
        assertThat(mRemovedCalls, is(1));
        assertThat(mManager.mToAddStore.getAllFences(), empty());
        assertThat(mManager.mSyncedStore.getAllFences(), hasSize(2));
        assertThat(mManager.mSyncedStore.getAllFences(), containsInAnyOrder(fence2, fence3));
        assertThat(mManager.mToRemoveStore.getAllFenceIds(), empty());
        assertThat(mManager.getFence("fenceId1"), nullValue());
        assertThat(mManager.getFence("fenceId2"), is(fence2));
        assertThat(mManager.getFence("fenceId3"), is(fence3));
    }

    private final MockSharedPreferences mPref = new MockSharedPreferences();

    private final Context mContext = new MockContext() {
        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return mPref;
        }
    };

    private static class MockGapiFenceManager extends GapiFenceManager {

        public boolean isConnected;
        public final HashMap<String, ResultCallback<Status>> addResultDict = new HashMap<>();
        public final HashMap<String, ResultCallback<Status>> removeResultDict = new HashMap<>();

        public MockGapiFenceManager(@NonNull Context context) {
            super(context);
        }

        @Override
        protected GoogleApiClient createGapi() {
            return null;
        }

        @Override
        boolean addFence(@NonNull String id, @NonNull AwarenessFence fence, @NonNull String pendingIntentClassName, ResultCallback<Status> status) {
            if (isConnected) {
                addResultDict.put(id, status);
            }
            return isConnected();
        }

        @Override
        boolean removeFence(@NonNull String fenceId, ResultCallback<Status> status) {
            if (isConnected) {
                removeResultDict.put(fenceId, status);
            }
            return isConnected();
        }

        @Override
        public boolean isConnected() {
            return isConnected;
        }

        @Override
        void connect() {
        }

        void setIsConnected() {
            isConnected = true;
            super.onConnected(null);
        }
    }

    private static class MockStorableFenceManager extends StorableFenceManager {

        public static MockGapiFenceManager sMockGapiFenceManager;

        public MockStorableFenceManager(Context context) {
            super(context);
        }

        @Override
        protected GapiFenceManager createGapiFenceManager() {
            return sMockGapiFenceManager;
        }
    }
}