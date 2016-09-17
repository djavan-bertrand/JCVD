package com.sousoum.jcvd;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.test.mock.MockContext;

import com.google.android.gms.awareness.fence.AwarenessFence;
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

import static org.hamcrest.Matchers.empty;
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
        StorableFence fence = StorableHeadphoneFence.pluggingIn();
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
        StorableFence fence = StorableHeadphoneFence.pluggingIn();
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