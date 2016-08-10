package com.sousoum.jcvdexample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sousoum.jcvd.StorableActivityFence;
import com.sousoum.jcvd.StorableFence;
import com.sousoum.jcvd.StorableFenceManager;
import com.sousoum.jcvd.StorableLocationFence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



public class FenceChooserActivity extends AppCompatActivity implements OnMapReadyCallback, StorableFenceManager.Listener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private static final String TAG = "FenceChooserActivity";
    @StorableActivityFence.ActivityType
    private List<Integer> mActivityType;
    private LatLng mLocation;

    private GoogleMap mMap;
    private Marker mMarker;

    private View mRootView;

    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 0;


    private StorableFenceManager mGeofenceManager;
    private View mFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence_chooser);

        mRootView = findViewById(R.id.parent_layout);

        MapFragment map = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        map.getMapAsync(this);

        mActivityType = new ArrayList<>();

        mGeofenceManager = new StorableFenceManager(this);
        mGeofenceManager.setListener(this);

        mFab = findViewById(R.id.fab);
        if (mFab != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StorableFence locationFence = null;
                    StorableFence activityFence = null;
                    StorableFence resultFence = null;
                    if (mLocation != null) {
                        locationFence = StorableLocationFence.entering(mLocation.latitude,
                                        mLocation.longitude, 100);
                    }

                    if (!mActivityType.isEmpty()) {
                        @StorableActivityFence.ActivityType
                        int[] ret = new int[mActivityType.size()];
                        int i = 0;
                        for (int val : mActivityType)
                            ret[i++] = val;
                        activityFence = StorableActivityFence.during(ret);
                    }

                    if (locationFence != null && activityFence != null) {
                        resultFence = StorableFence.and(locationFence, activityFence);
                    } else if (locationFence != null) {
                        resultFence = locationFence;
                    } else if (activityFence != null) {
                        resultFence = activityFence;
                    }
                    if (resultFence != null) {
                        boolean addedOnGoing = false;
                        if (ContextCompat.checkSelfPermission(FenceChooserActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            addedOnGoing = mGeofenceManager.addFence(UUID.randomUUID().toString(), resultFence, null, CustomTransitionsIntentService.class.getName());
                        }
                        if (!addedOnGoing) {
                            Log.e(TAG, "Addition of fence has been refused " + resultFence);
                        }
                    }

                }
            });
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_REQUEST_CODE);
        } else {
            onAccessFineLocationPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onAccessFineLocationPermissionGranted();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, FenceListActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMapLongClickListener(this);
    }

    private void onAccessFineLocationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mFab.setVisibility(View.VISIBLE);
            Snackbar snackbar = Snackbar
                    .make(mRootView, getString(R.string.add_fence), Snackbar.LENGTH_LONG);

            snackbar.show();
        }
    }

    public void onWalkingClicked(View view) {
        if (mActivityType.contains(StorableActivityFence.WALKING)) {
            mActivityType.remove(Integer.valueOf(StorableActivityFence.WALKING));
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            mActivityType.add(StorableActivityFence.WALKING);
            view.setBackgroundColor(Color.WHITE);
        }
    }

    public void onRunningClicked(View view) {
        if (mActivityType.contains(StorableActivityFence.RUNNING)) {
            mActivityType.remove(Integer.valueOf(StorableActivityFence.RUNNING));
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            mActivityType.add(StorableActivityFence.RUNNING);
            view.setBackgroundColor(Color.WHITE);
        }
    }

    public void onDrivingClicked(View view) {
        if (mActivityType.contains(StorableActivityFence.IN_VEHICLE)) {
            mActivityType.remove(Integer.valueOf(StorableActivityFence.IN_VEHICLE));
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            mActivityType.add(StorableActivityFence.IN_VEHICLE);
            view.setBackgroundColor(Color.WHITE);
        }
    }

    public void onBicyclingClicked(View view) {
        if (mActivityType.contains(StorableActivityFence.ON_BICYCLE)) {
            mActivityType.remove(Integer.valueOf(StorableActivityFence.ON_BICYCLE));
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            mActivityType.add(StorableActivityFence.ON_BICYCLE);
            view.setBackgroundColor(Color.WHITE);
        }
    }

    public void onStillClicked(View view) {
        if (mActivityType.contains(StorableActivityFence.STILL)) {
            mActivityType.remove(Integer.valueOf(StorableActivityFence.STILL));
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            mActivityType.add(StorableActivityFence.STILL);
            view.setBackgroundColor(Color.WHITE);
        }
    }

    public void onFootClicked(View view) {
        if (mActivityType.contains(StorableActivityFence.ON_FOOT)) {
            mActivityType.remove(Integer.valueOf(StorableActivityFence.ON_FOOT));
            view.setBackgroundColor(Color.LTGRAY);
        } else {
            mActivityType.add(StorableActivityFence.ON_FOOT);
            view.setBackgroundColor(Color.WHITE);
        }
    }

    //region StorableGeofenceListener
    @Override
    public void fenceAddStatus(StorableFence fence, Status status) {
        if (fence != null) {
            if (status.isSuccess()) {
                Toast.makeText(this, "Geofence " + fence.getId() + " has been added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error when adding " + fence.getId() + " : " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void fenceRemoveStatus(String geofenceId, Status status) {
        if (status.isSuccess()) {
            Toast.makeText(this, "Geofence " + geofenceId + " has been removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error when removing " + geofenceId + " : " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mLocation = latLng;
        if (mMarker != null) {
            mMarker.remove();
        }
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mLocation = null;
        if (mMarker != null) {
            mMarker.remove();
        }
    }
    //endregion StorableGeofenceListener
}
