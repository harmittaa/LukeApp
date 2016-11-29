package com.luke.lukef.lukeapp.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.SubmissionDatabase;
import com.luke.lukef.lukeapp.model.SubmissionMarker;
import com.luke.lukef.lukeapp.tools.PopupMaker;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the Map view, fetches submission
 */
public class MapViewFragment extends Fragment implements View.OnClickListener, LocationListener, OnMapReadyCallback, OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener,
        ClusterManager.OnClusterClickListener<SubmissionMarker>,
        ClusterManager.OnClusterItemClickListener<SubmissionMarker> {

    private static final String TAG = "MapViewFragment";
    private View fragmentView;
    private Button leaderboardButton;
    Location lastLoc;
    Location lastKnownLoc;
    GoogleMap googleMap;
    private ClusterManager<SubmissionMarker> clusterManager;
    private ClusterManager<SubmissionMarker> adminClusterManager;
    private MapFragment mapFragment;
    private VisibleRegion visibleRegion;
    private List<String> submissionMarkerIdList;

    public Location getLastLoc() {
        if (this.lastLoc != null) {
            Location jeeben = new Location("");
            jeeben.setAltitude(this.lastLoc.getAltitude());
            jeeben.setLatitude(this.lastLoc.getLatitude());
            jeeben.setLongitude(this.lastLoc.getLongitude());
            return jeeben;
        } else if (this.lastKnownLoc != null) {
            Location jeeben = new Location("");
            jeeben.setAltitude(this.lastKnownLoc.getAltitude());
            jeeben.setLatitude(this.lastKnownLoc.getLatitude());
            jeeben.setLongitude(this.lastKnownLoc.getLongitude());
            return jeeben;
        } else {
            return null;
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
        leaderboardButton = (Button) fragmentView.findViewById(R.id.leaderboard_button);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.MAP_CAMERA);
        setupGoogleMap();
        this.submissionMarkerIdList = new ArrayList<>();
        mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.leaderboard_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_LEADERBOARD, null);
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        leaderboardButton.setOnClickListener(this);
    }

    /**
     * Setup method for open street map in this fragment
     * Enables touch controls
     * Sets starting position and zooms in
     */
    private void setupGoogleMap() {
        //init map
        //get current phone position and zoom to location
        LocationManager lm = (LocationManager) getMainActivity().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: 21/11/2016 ask for permission
        }

        this.lastKnownLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

    }

    private void zoomMap() {
        // TODO: 27/11/2016 Check permission, so no crash
        /*CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(getLastLoc().getLatitude(), getLastLoc().getLongitude()));
        CameraUpdate cu = CameraUpdateFactory.zoomTo(15);
        googleMap.moveCamera(center);
        googleMap.animateCamera(cu);

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));*/

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(getLastLoc().getLatitude(), getLastLoc().getLongitude()))      // Sets the center of the map to Mountain View
                .zoom(17)                  // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setupClustering();
        addAdminMarkersToMap();
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        zoomMap();
        if (ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.googleMap.setMyLocationEnabled(true);
    }

    /**
     * Setup method for Marker clustering
     */
    private void setupClustering() {
        this.clusterManager = new ClusterManager<SubmissionMarker>(getActivity(), this.googleMap);
        this.adminClusterManager = new ClusterManager<SubmissionMarker>(getActivity(), this.googleMap);
        CompositeOnCameraIdleListener compositeOnCameraIdleListener = new CompositeOnCameraIdleListener();
        CompositeOnMarkerClickListener compositeOnMarkerClickListener = new CompositeOnMarkerClickListener();
        googleMap.setOnCameraIdleListener(compositeOnCameraIdleListener);
        googleMap.setOnMarkerClickListener(compositeOnMarkerClickListener);
        compositeOnCameraIdleListener.registerListener(this.clusterManager);
        compositeOnCameraIdleListener.registerListener(this);
        compositeOnMarkerClickListener.registerMarkerOnClickListener(this.clusterManager);
        compositeOnMarkerClickListener.registerMarkerOnClickListener(this);
        this.clusterManager.setOnClusterClickListener(this);
        this.clusterManager.setOnClusterItemClickListener(this);
    }

    /**
     * Gets called when user has stopped moving the map
     */
    @Override
    public void onCameraIdle() {
        if (this.googleMap != null) {
            if (this.visibleRegion == null) {
                this.visibleRegion = this.googleMap.getProjection().getVisibleRegion();
                addSubmissionsToMap(this.visibleRegion);
            } else {
                // TODO: 29/11/2016 check here if the camera has moved enough to get new stuff from the DB or not
                this.visibleRegion = this.googleMap.getProjection().getVisibleRegion();
                addSubmissionsToMap(this.visibleRegion);
            }
        }
    }

    /**
     * Adds admin markers into the map as basic markers that won't be grouped.
     * Calls the SQLite {@link SubmissionDatabase#queryAdminMarkers()} for a cursor with admin markers
     */
    private void addAdminMarkersToMap() {
        SubmissionDatabase submissionDatabase = new SubmissionDatabase(getActivity());
        Cursor queryCursor = submissionDatabase.queryAdminMarkers();
        queryCursor.moveToFirst();
        if (queryCursor.getCount() > 0) {
            Log.e(TAG, "addAdminMarkersToMap: AdminMarkers query amount is " + queryCursor.getCount());
            while (queryCursor.moveToNext()) {
                LatLng adminMarkerLatLng = new LatLng(queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("admin_marker_longitude")),
                        queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("admin_marker_latitude")));
                this.googleMap.addMarker(new MarkerOptions()
                        .position(adminMarkerLatLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title(queryCursor.getString(queryCursor.getColumnIndexOrThrow("admin_marker_title"))));
                Log.e(TAG, "addAdminMarkersToMap: Added marker");
            }
        }
    }

    /**
     * Adds submissions to the map based on the provided <code>VisibleRegion</code>. Passes the VisibleRegion
     * to the {@link com.luke.lukef.lukeapp.SubmissionDatabase#querySubmissions(VisibleRegion visibleRegion)}
     *
     * @param visibleRegion The region currently visible on the map
     */
    private void addSubmissionsToMap(VisibleRegion visibleRegion) {
        SubmissionDatabase submissionDatabase = new SubmissionDatabase(getActivity());
        Cursor queryCursor = submissionDatabase.querySubmissions(visibleRegion);
        queryCursor.moveToFirst();
        if (queryCursor.getCount() > 0) {
            Log.e(TAG, "addSubmissionsToMap: Submissions amount is "+queryCursor.getCount() );
            while (queryCursor.moveToNext()) {
                if (!this.submissionMarkerIdList.contains(queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_id")))) {
                    SubmissionMarker submissionMarker = new SubmissionMarker(
                            queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_id")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("submission_latitude")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("submission_longitude"))
                    );
                    Log.e(TAG, "addSubmissionsToMap: Added submission");
                    this.submissionMarkerIdList.add(queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_id")));
                    this.clusterManager.addItem(submissionMarker);
                } else {
                   // Log.e(TAG, "addSubmissionsToMap: Submission already on the map");
                }
            }
            this.clusterManager.cluster();
            submissionDatabase.closeDbConnection();
        }
    }


    @Override
    public boolean onClusterClick(Cluster<SubmissionMarker> cluster) {
        Log.e(TAG, "onClusterClick: Cluster clicked");
        return false;
    }

    @Override
    public boolean onClusterItemClick(SubmissionMarker submissionMarker) {
        Log.e(TAG, "onClusterItemClick: Cluster item clicked");
        System.out.println("OnClusterItemClick");
        PopupMaker popMaker = new PopupMaker(getMainActivity());
        popMaker.createPopupTest();
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "onMarkerClick: marker clicked");
/*        if (marker.getTitle().isEmpty()) {
            PopupMaker popMaker = new PopupMaker(getMainActivity());
            popMaker.createPopupTest();
        }*/
        return false;
    }

    /**
     * Provides objects possibility to listen to OnCameraIdle events by calling {@link #registerListener(OnCameraIdleListener listener)}
     */
    class CompositeOnCameraIdleListener implements OnCameraIdleListener {
        private List<OnCameraIdleListener> registeredListeners = new ArrayList<>();

        /**
         * Adds OnCameraIdleListener type object to the <code>List<OnCameraIdleListener> registeredListeners</code>
         *
         * @param listener OnCameraIdleListener type object
         */
        void registerListener(OnCameraIdleListener listener) {
            registeredListeners.add(listener);
        }

        @Override
        public void onCameraIdle() {
            // loop through listeners and call their onCameraIdle method()
            for (int i = 0; i < registeredListeners.size(); i++) {
                registeredListeners.get(i).onCameraIdle();
            }
        }
    }

    /**
     * Provides objects possibility to listen to OnCameraIdle events by calling
     * {@link #registerMarkerOnClickListener(GoogleMap.OnMarkerClickListener)}.
     */
    class CompositeOnMarkerClickListener implements GoogleMap.OnMarkerClickListener {
        private List<GoogleMap.OnMarkerClickListener> registeredListeners = new ArrayList<>();

        /**
         * Adds OnMarkerClickListener type object to the <code>List<OnMarkerClickListener> registeredListeners</code>
         *
         * @param listener OnCameraIdleListener type object
         */
        void registerMarkerOnClickListener(GoogleMap.OnMarkerClickListener listener) {
            registeredListeners.add(listener);
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            // loop through listeners and call their onCameraIdle method()
            for (int i = 0; i < registeredListeners.size(); i++) {
                registeredListeners.get(i).onMarkerClick(marker);
            }
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLoc = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
