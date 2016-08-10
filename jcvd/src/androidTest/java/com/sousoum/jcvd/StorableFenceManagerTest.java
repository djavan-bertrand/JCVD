package com.sousoum.jcvd;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.test.mock.MockContext;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.sousoum.jcvd.mocks.MockSharedPreferences;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@RunWith(android.support.test.runner.AndroidJUnit4.class)
public class StorableFenceManagerTest extends TestCase {

    private StorableFenceManager mManager;
    private MockGoogleApiClient mMockGapi;

    @Before
    public void setUp() throws Exception {
        mMockGapi = new MockGoogleApiClient();
        mManager = new MockStorableFenceManager(mContext, mMockGapi);
    }

    @Test
    public void testDefault() {
        assertThat( mManager.getAllFences(), empty());
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

    private static class MockStorableFenceManager extends StorableFenceManager {

        private static MockGoogleApiClient mMockGapi;

        public MockStorableFenceManager(Context context, MockGoogleApiClient mockGapi) {
            super(context);
            mMockGapi = mockGapi;
        }

        @Override
        protected GoogleApiClient createGapi(Context context) {
            return mMockGapi;
        }
    }

    private static class MockGoogleApiClient extends GoogleApiClient {

        @Override
        public boolean hasConnectedApi(@NonNull Api<?> api) {
            return false;
        }

        @NonNull
        @Override
        public ConnectionResult getConnectionResult(@NonNull Api<?> api) {
            return null;
        }

        @Override
        public void connect() {

        }

        @Override
        public ConnectionResult blockingConnect() {
            return null;
        }

        @Override
        public ConnectionResult blockingConnect(long l, @NonNull TimeUnit timeUnit) {
            return null;
        }

        @Override
        public void disconnect() {

        }

        @Override
        public void reconnect() {

        }

        @Override
        public PendingResult<Status> clearDefaultAccountAndReconnect() {
            return null;
        }

        @Override
        public void stopAutoManage(@NonNull FragmentActivity fragmentActivity) {

        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public boolean isConnecting() {
            return false;
        }

        @Override
        public void registerConnectionCallbacks(@NonNull ConnectionCallbacks connectionCallbacks) {

        }

        @Override
        public boolean isConnectionCallbacksRegistered(@NonNull ConnectionCallbacks connectionCallbacks) {
            return false;
        }

        @Override
        public void unregisterConnectionCallbacks(@NonNull ConnectionCallbacks connectionCallbacks) {

        }

        @Override
        public void registerConnectionFailedListener(@NonNull OnConnectionFailedListener onConnectionFailedListener) {

        }

        @Override
        public boolean isConnectionFailedListenerRegistered(@NonNull OnConnectionFailedListener onConnectionFailedListener) {
            return false;
        }

        @Override
        public void unregisterConnectionFailedListener(@NonNull OnConnectionFailedListener onConnectionFailedListener) {

        }

        @Override
        public void dump(String s, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strings) {

        }
    }
}