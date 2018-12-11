package com.sousoum.jcvdexample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.sousoum.jcvd.StorableActivityFence;
import com.sousoum.jcvd.StorableFence;
import com.sousoum.jcvd.StorableFenceManager;
import com.sousoum.jcvd.StorableLocationFence;

import java.util.ArrayList;

/**
 * Created by d.bertrand on 13/07/2016.
 */
public class FenceRecyclerAdapter extends RecyclerView.Adapter<FenceRecyclerAdapter.ViewHolder>  {

    private static final String TAG = "MGRecyclerAdapter";
    private final ArrayList<StorableFence> mFences;
    private final StorableFenceManager mFenceManager;
    private Context mContext;

    public FenceRecyclerAdapter(Context context, StorableFenceManager fenceManager) {
        mContext = context;
        mFences = new ArrayList<>();

        mFenceManager = fenceManager;

        updateMetroGeofenceList();
    }

    public StorableFence getItem(int position) {
        StorableFence item = null;
        if (position >= 0 && position < mFences.size()) {
            item = mFences.get(position);
        }

        return item;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(mContext).inflate(R.layout.fence_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StorableFence metroGeofence = getItem(position);
        holder.setFence(metroGeofence);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mFences.size();
    }

    public void updateMetroGeofenceList() {
        mFences.clear();

        ArrayList<StorableFence> storableGeofences = mFenceManager.getAllFences();
        mFences.addAll(storableGeofences);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private StorableFence mFence;

        private final TextView mTextView;
        private final MapView mMapView;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(R.id.text);
            mMapView = (MapView)itemView.findViewById(R.id.map);
            itemView.setOnClickListener(mClickListener);
        }

        public void setFence(StorableFence fence) {
            mFence = fence;
            boolean hasLocationFence = false;
            String text = fence.getId() + "\n";
            if (!fence.getAndFences().isEmpty()) {
                for (StorableFence andFence : fence.getAndFences()) {
                    if (!text.isEmpty()) {
                        text += " && ";
                    }
                    text += getFenceStr(andFence);
                    if (andFence.getType().equals(StorableFence.Type.LOCATION)) {
                        hasLocationFence = true;
                    }
                }
            } else {
                text += getFenceStr(fence);
                if (fence.getType().equals(StorableFence.Type.LOCATION)) {
                    hasLocationFence = true;
                }
            }
            mTextView.setText(text);

            if (hasLocationFence) {
                mMapView.setVisibility(View.VISIBLE);
                mMapView.onCreate(null);
                mMapView.onResume();
                mMapView.getMapAsync(mMapReadyCallback);
                mMapView.setClickable(false);
            } else {
                mMapView.setVisibility(View.GONE);
            }
        }

        public String getFenceStr(StorableFence fence) {
            String str = "";
            switch (fence.getType()) {
                case ACTIVITY:
                    for (@StorableActivityFence.ActivityType int act : ((StorableActivityFence) fence).getActivityTypes()) {
                        if (!str.isEmpty()) {
                            str += ", ";
                        }
                        switch (act) {
                            case DetectedActivityFence.IN_VEHICLE:
                                str += "IN_VEHICLE";
                                break;
                            case DetectedActivityFence.ON_BICYCLE:
                                str += "ON_BICYCLE";
                                break;
                            case DetectedActivityFence.ON_FOOT:
                                str += "ON_FOOT";
                                break;
                            case DetectedActivityFence.RUNNING:
                                str += "RUNNING";
                                break;
                            case DetectedActivityFence.STILL:
                                str += "STILL";
                                break;
                            case DetectedActivityFence.WALKING:
                                str += "WALKING";
                                break;
                            case DetectedActivityFence.UNKNOWN:
                            default:
                                str += "UNKNOWN";
                                break;
                        }
                    }
                    break;
                case LOCATION:
                    StorableLocationFence locFence = (StorableLocationFence) fence;
                    str += "(" + locFence.getLatitude() + ", " + locFence.getLongitude() + ") ";
                    break;
                default:
                    break;
            }
            return str;
        }

        private final OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                StorableLocationFence locFence = null;
                if (!mFence.getAndFences().isEmpty()) {
                    for (StorableFence andFence : mFence.getAndFences()) {
                        if (andFence.getType().equals(StorableFence.Type.LOCATION)) {
                            locFence = (StorableLocationFence) andFence;
                        }
                    }
                } else {
                    if (mFence.getType().equals(StorableFence.Type.LOCATION)) {
                        locFence = (StorableLocationFence) mFence;
                    }
                }
                if (locFence != null) {
                    LatLng latLng = new LatLng(locFence.getLatitude(), locFence.getLongitude());
                    CircleOptions circleOptions = new CircleOptions()
                            .center(latLng)
                            .radius(locFence.getRadius()); // In meters

                    googleMap.addCircle(circleOptions);

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(14)
                            .build();
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        };
        /*private final View.OnClickListener mClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(TAG, "Item has been clicked : " + mMetroGeofence.getStorableGeofence().getId());
                if (mExpandableView.getVisibility() == View.GONE) {
                    mExpandableView.setVisibility(View.VISIBLE);
                    mExpandableView.onCreate(null);
                    mExpandableView.onResume();
                    mExpandableView.getMapAsync(mMapReadyCallback);
                } else {
                    mExpandableView.setVisibility(View.GONE);
                    mExpandableView.onPause();
                }

            }*/

        private final View.OnClickListener mClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO; get the state of the fence
                //mFenceManager.getFenceState(mFence);
            }
        };
    }
}
