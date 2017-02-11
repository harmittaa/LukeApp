/*
        BalticApp, for studying and tracking the condition of the Baltic sea
        and Gulf of Finland throug user submissions.
        Copyright (C) 2016  Daniel Zakharin, LuKe

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/> or
        the beginning of MainActivity.java file.

*/

package com.luke.lukef.lukeapp.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;
import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;

import com.luke.lukef.lukeapp.model.ClusterMarkerAdmin;
import com.luke.lukef.lukeapp.popups.AdminMarkerPopup;
import com.luke.lukef.lukeapp.tools.SubmissionDatabase;
import com.luke.lukef.lukeapp.WelcomeActivity;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.model.ClusterMarker;
import com.luke.lukef.lukeapp.popups.SubmissionPopup;
import com.luke.lukef.lukeapp.tools.LukeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Handles the Map view, fetches submissions, populates map with Submissions and Admin markers
 */

// TODO: 25.1.2017 Add ContextCompat to MainActivity so it can work in older android version
public class MapViewFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, OnCameraIdleListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener,
        GoogleMap.OnMarkerClickListener,
        ClusterManager.OnClusterClickListener<ClusterMarker>,
        ClusterManager.OnClusterItemClickListener<ClusterMarker> {
    private static final String TAG = "MapViewFragment";
    private View fragmentView;
    private Location lastLoc;
    private Location lastKnownLoc;
    private GoogleMap googleMap;
    private ClusterManager<ClusterMarker> clusterManager;
    private MapFragment mapFragment;
    private VisibleRegion visibleRegion;
    private List<String> submissionMarkerIdList;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private ImageButton menuButton;
    private ImageButton filtersButon;
    private ImageButton newSubmissionButton;
    private SubmissionPopup submissionPopup;
    private long minDateInMs = 0;
    private int tempY;
    private int tempM;
    private int tempD;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.submissionMarkerIdList = new ArrayList<>();

        connectToGoogleApi();
        setupGoogleMap();
        createLocationRequest();

        if (this.fragmentView != null) {
            ViewGroup parent = (ViewGroup) this.fragmentView.getParent();
            if (parent != null)
                parent.removeView(this.fragmentView);
        }
        try {
            if(this.fragmentView == null)
            this.fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
            this.mapFragment = ((MapFragment) getChildFragmentManager().findFragmentById(R.id.googleMapFragment));
            //check if fragment is null. On API 19 childfragmentmanager returns null, so regular fragment manager is used
            if (this.mapFragment == null) {
                this.mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.googleMapFragment));
            }
            this.mapFragment.getMapAsync(this);

        } catch (InflateException e) {
            Log.e(TAG, "onCreateView: ", e);
            //this.mapFragment.getMapAsync(this);
        }
        setupButtons();
        return this.fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //this prevents crash on older devices due to duplicate fragments
        MapFragment f = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.googleMapFragment);
        if (f != null)
            getFragmentManager().beginTransaction().remove(f).commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                getMainActivity().openDrawer();
                break;
            case R.id.button_filters:
                showCalendarPicker();
                break;
            case R.id.button_new_submission:
                activateNewSubmission();
                break;
        }
    }

    /**
     * Returns the previously known user location, either the last location or last known location
     *
     * @return Location object, either long press location, last known location or null if no location is available.
     */
    private Location getLastLoc() {
        Location location = new Location("");
        if (this.lastLoc != null) {
            location.setAltitude(this.lastLoc.getAltitude());
            location.setLatitude(this.lastLoc.getLatitude());
            location.setLongitude(this.lastLoc.getLongitude());
            return location;
        } else if (this.lastKnownLoc != null) {
            location.setAltitude(this.lastKnownLoc.getAltitude());
            location.setLatitude(this.lastKnownLoc.getLatitude());
            location.setLongitude(this.lastKnownLoc.getLongitude());
            return location;
        } else {
            return null;
        }
    }


    /**
     * Switches to {@link NewSubmissionFragment} if user is logged in, GPS is on and there is a internet connection. Else
     * shows pop up to login if not.
     */
    private void activateNewSubmission() {
        if (SessionSingleton.getInstance().isUserLogged()) {
            if (LukeUtils.checkGpsStatus(getMainActivity())) {
                if (LukeUtils.checkInternetStatus(getMainActivity())) {
                    switchToNewSubmissions(constructBundle(getLastLoc()));
                } else {
                    // TODO: 21.1.2017 prompt user to turn on gps / internet
                    getMainActivity().makeToast("No internet connection");
                }
            } else {
                getMainActivity().makeToast("Activate GPS");
            }
        } else {
            createLoginPrompt();
        }
    }

    private void switchToNewSubmissions(Bundle bundle) {
        getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_NEW_SUBMISSION, bundle);
    }

    /**
     * Creates a {@link Bundle} object with doubles longitude, latitude and altitude.
     *
     * @param location Location object from which longitude, latitude and altitude are fetched
     *                 to be placed in the Bundle.
     * @return Bundle with double type values longitude, latitude and altitude.
     */
    private Bundle constructBundle(Location location) {
        Bundle bundle = new Bundle();
        bundle.putDouble("latitude", location.getLatitude());
        bundle.putDouble("longitude", location.getLongitude());
        bundle.putDouble("altitude", location.getAltitude());
        return bundle;
    }

    /**
     * Creates a login prompt with <b>Login</b> and <b>Cancel</b> buttons. Login button takes user to the starting screen
     */
    // TODO: 21.1.2017 change the style of the prompt to match style of the App
    // TODO: 21.1.2017 move to LukeUtils
    private void createLoginPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        builder.setMessage("Please Log in to Submit")
                .setCancelable(false)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().startActivity(new Intent(getActivity().getApplicationContext(), WelcomeActivity.class));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Unhides the submissions popup
     */
    public void unhidePopup() {
        this.submissionPopup.unHidePopup();
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    /**
     * Handles setting up and click listeners
     */
    private void setupButtons() {
        this.filtersButon = (ImageButton) this.fragmentView.findViewById(R.id.button_filters);
        this.newSubmissionButton = (ImageButton) this.fragmentView.findViewById(R.id.button_new_submission);
        this.menuButton = (ImageButton) this.fragmentView.findViewById(R.id.button_back);
        this.filtersButon.setOnClickListener(this);
        this.newSubmissionButton.setOnClickListener(this);
        this.menuButton.setOnClickListener(this);
    }

    /**
     * Sets up the Google Map Fragment
     */
    private void setupGoogleMap() {
        LocationManager lm = (LocationManager) getMainActivity().getSystemService(Context.LOCATION_SERVICE);
        //check for permissions and get last known location of the device
        if (ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getMainActivity().checkPermissions();
            this.lastKnownLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } else {
            this.lastKnownLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
    }

    /**
     * Zooms the map on the last known location
     */
    private void zoomMap() {
        Location location = fetchArgsFromBundle();
        if (location != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to Mountain View
                    .zoom(17)                  // Sets the tilt of the luke_camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    /**
     * Fetches arguments from given bundle or calls {@link MapViewFragment#getLastLoc()} for
     * previously known location.
     *
     * @return {@link Location} object
     */
    private Location fetchArgsFromBundle() {
        Bundle b = getArguments();
        if (b != null && b.containsKey("latitude")) {
            Location location = new Location("jes");
            location.setLatitude(b.getDouble("latitude"));
            location.setLongitude(b.getDouble("longitude"));
            getArguments().clear();
            return location;
        } else if (getLastLoc() != null) {
            Location location = new Location("jes");
            location.setLatitude(getLastLoc().getLatitude());
            location.setLongitude(getLastLoc().getLongitude());
            return location;
        } else {
            return null;
        }
    }

    /**
     * Setup method for Marker clustering
     */
    private void setupClustering() {
        if (this.clusterManager == null) {
            this.clusterManager = new ClusterManager<>(getActivity(), this.googleMap);
            CompositeOnCameraIdleListener compositeOnCameraIdleListener = new CompositeOnCameraIdleListener();
            CompositeOnMarkerClickListener compositeOnMarkerClickListener = new CompositeOnMarkerClickListener();
            this.googleMap.setOnCameraIdleListener(compositeOnCameraIdleListener);
            this.googleMap.setOnMarkerClickListener(compositeOnMarkerClickListener);
            compositeOnCameraIdleListener.registerListener(this.clusterManager);
            compositeOnCameraIdleListener.registerListener(this);
            compositeOnMarkerClickListener.registerMarkerOnClickListener(this.clusterManager);
            compositeOnMarkerClickListener.registerMarkerOnClickListener(this);
            this.clusterManager.setOnClusterClickListener(this);
            this.clusterManager.setOnClusterItemClickListener(this);
            this.clusterManager.setRenderer(new MarkerRenderer(getActivity(), this.googleMap, this.clusterManager));
        } else {
            this.clusterManager.clearItems();
            this.submissionMarkerIdList.clear();
        }
    }

    /**
     * Handles connecting to the Google API
     */
    private void connectToGoogleApi() {
        this.googleApiClient = new GoogleApiClient.Builder(getMainActivity()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        this.googleApiClient.connect();
    }

    /**
     * Makes a locationrequest to track the location of the user on the map
     */
    protected void createLocationRequest() {
        this.locationRequest = new LocationRequest();
        this.locationRequest.setInterval(5000);
        this.locationRequest.setFastestInterval(2000);
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Adds {@link ClusterMarker} admin markers into the map as basic markers that won't be grouped.
     * Calls the SQLite {@link SubmissionDatabase#queryAdminMarkers()} for a cursor with admin markers
     */
    private void addAdminMarkersToMap() {
        SubmissionDatabase submissionDatabase = new SubmissionDatabase(getActivity());
        Cursor queryCursor = submissionDatabase.queryAdminMarkers();
        queryCursor.moveToFirst();
        if (queryCursor.getCount() > 0) {
            do {
                if (!this.submissionMarkerIdList.contains(queryCursor.getString(queryCursor.getColumnIndexOrThrow("admin_marker_id")))) {
                    ClusterMarkerAdmin adminMarker = new ClusterMarkerAdmin(
                            queryCursor.getString(queryCursor.getColumnIndexOrThrow("admin_marker_id")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("admin_marker_latitude")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("admin_marker_longitude")),
                            queryCursor.getString(queryCursor.getColumnIndexOrThrow("admin_marker_title")),
                            "");
                    this.submissionMarkerIdList.add(queryCursor.getString(queryCursor.getColumnIndexOrThrow("admin_marker_id")));
                    this.clusterManager.addItem(adminMarker);
                }

            } while (queryCursor.moveToNext());
            this.clusterManager.cluster();
        }
        submissionDatabase.closeDbConnection();
    }

    /**
     * Adds submissions to the map based on the provided <code>VisibleRegion</code>. Passes the VisibleRegion
     * to the {@link SubmissionDatabase#querySubmissions(VisibleRegion, Long)}
     *
     * @param visibleRegion The region currently visible on the map
     */
    private void addSubmissionsToMap(VisibleRegion visibleRegion) {
        SubmissionDatabase submissionDatabase = new SubmissionDatabase(getActivity());
        Cursor queryCursor;
        if (this.minDateInMs > 0) {
            queryCursor = submissionDatabase.querySubmissions(visibleRegion, this.minDateInMs);
        } else {
            queryCursor = submissionDatabase.querySubmissions(visibleRegion, null);
        }
        queryCursor.moveToFirst();
        if (queryCursor.getCount() > 0) {
            do {
                if (!this.submissionMarkerIdList.contains(queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_id")))) {
                    ClusterMarker clusterMarker = new ClusterMarker(
                            queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_id")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("submission_latitude")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("submission_longitude")),
                            "",
                            queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_positive")));
                    this.submissionMarkerIdList.add(queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_id")));
                    this.clusterManager.addItem(clusterMarker);
                }
            } while (queryCursor.moveToNext());
            this.clusterManager.cluster();
        }
        submissionDatabase.closeDbConnection();
    }

    /**
     * Handles showing the Calendar pop up, fetching the selected date, calling to fetch
     * submissions again
     */
    private void showCalendarPicker() {
        // Inflate the popup_layout.xml
        ConstraintLayout viewGroup = (ConstraintLayout) getMainActivity().findViewById(R.id.popup_calendar_root);
        LayoutInflater layoutInflater = (LayoutInflater) getMainActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.popup_calendar, viewGroup);
        // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
        //or if popup is on edge display it to the left of the circle
        Display display = getMainActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point(0, 0);
        display.getSize(size);

        int OFFSET_X = 25;
        int OFFSET_Y = 25;

        final DatePicker dP = (DatePicker) layout.findViewById(R.id.popup_calendar_datepicker);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(getMainActivity());
        popup.setAnimationStyle(android.R.style.Animation_Dialog);
        popup.setContentView(layout);

        popup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        popup.setFocusable(true);
        //gets rid of default background
        popup.setBackgroundDrawable(new BitmapDrawable(getMainActivity().getResources(), (Bitmap) null));
        //popup.setBackgroundDrawable(new BitmapDrawable(getMainActivity().getResources(), (Bitmap) nu));

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, 200 + OFFSET_X, 300 + OFFSET_Y);
        Calendar minDate;
        minDate = Calendar.getInstance();
        this.tempY = minDate.get(Calendar.YEAR);
        this.tempM = minDate.get(Calendar.MONTH);
        this.tempD = minDate.get(Calendar.DAY_OF_MONTH);
        dP.init(minDate.get(Calendar.YEAR), minDate.get(Calendar.MONTH), minDate.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            // Months start from 0, so January is month 0
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                tempY = year;
                tempM = monthOfYear;
                tempD = dayOfMonth;
                Log.e(TAG, "onDateChanged: selected " + tempD + " " + tempM + " " + tempY);
            }
        });
        ImageButton okButton = (ImageButton) layout.findViewById(R.id.popup_calendar_accept);
        ImageButton cancelButton = (ImageButton) layout.findViewById(R.id.popup_calendar_cancel);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(tempY, tempM, tempD, 1, 0);
                Log.e(TAG, "onClick: calendar time in ms " + calendar.getTimeInMillis());
                // clear items from clustermanager and submissionMarkerList, as all new submissions
                // need to be fetched based on the selected date
                clusterManager.clearItems();
                submissionMarkerIdList.clear();
                addAdminMarkersToMap();
                setMinDateInMs(calendar.getTimeInMillis());
                popup.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMinDateInMs(0);
                popup.dismiss();

            }
        });
    }

    /**
     * Setter for the minDateInMs which is the minimum date of which submissions should be shown
     *
     * @param minDateInMs The minimum date of which submissions are shown, in MS
     */
    private void setMinDateInMs(long minDateInMs) {
        this.minDateInMs = minDateInMs;
        if (this.minDateInMs > 0) {
            addSubmissionsToMap(this.googleMap.getProjection().getVisibleRegion());
        } else {
            addSubmissionsToMap(this.googleMap.getProjection().getVisibleRegion());
        }
    }

    @Override
    public boolean onClusterClick(Cluster<ClusterMarker> cluster) {
        // TODO: 12/12/2016 Zoom when clicking a cluster
     /*   // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

   *//*     CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(cluster.getPosition())      // Sets the center of the map to Mountain View
                .zoom(15)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
*//*

        Log.e(TAG, "onClusterClick: BOUNDS SW " + bounds.southwest + " Cneter " + bounds.getCenter());
        // Animate camera to the bounds
        try {
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            //this.googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            Log.e(TAG, "onClusterClick: animated");
        } catch (Exception e) {
            Log.e(TAG, "onClusterClick: animate failed ", e);
        }*/

        return false;
    }

    /**
     * Checks if the clicked submission is an admin marker and passes
     * correct parameters to SubmissionPopup.
     *
     * @param clusterMarker Clicked cluster marker
     * @return false
     */
    @Override
    public boolean onClusterItemClick(ClusterMarker clusterMarker) {
        boolean isAdminMarker = true;
        if (clusterMarker instanceof ClusterMarkerAdmin) {
            AdminMarkerPopup adminMarkerPopup = new AdminMarkerPopup(clusterMarker.getSubmissionId(), getMainActivity());
            adminMarkerPopup.createPopupAdmin();
        } else {
            //deprecated code left in because of time constraints, should remove isAdminmarker boolean from submissionpopup
            isAdminMarker = false;
            submissionPopup = new SubmissionPopup(getMainActivity());
            submissionPopup.createPopup(clusterMarker.getSubmissionId(), isAdminMarker);
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "onMarkerClick: marker clicked");
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setupClustering();
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.setOnCameraIdleListener(this);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        zoomMap();
        if (ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (SessionSingleton.getInstance().isUserLogged()) {
                    if (LukeUtils.checkInternetStatus(getMainActivity())) {
                        Location location = new Location("");
                        location.setLatitude(latLng.latitude);
                        location.setLongitude(latLng.longitude);
                        switchToNewSubmissions(constructBundle(location));
                    } else {
                        getMainActivity().makeToast("You need to log in to do this");
                    }
                } else {
                    createLoginPrompt();
                }
            }
        });
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Gets called when user has stopped moving the map, filters visibler submissions depending on the visible map area
     */
    @Override
    public void onCameraIdle() {
        if (this.googleMap != null) {
            if (this.visibleRegion == null) {
                // get visible region
                this.visibleRegion = this.googleMap.getProjection().getVisibleRegion();
                // if a filter has been set then pass the filter, if not the null
                if (this.minDateInMs > 0) {
                    addSubmissionsToMap(this.visibleRegion);
                } else {
                    addSubmissionsToMap(this.visibleRegion);
                }
                // adds admin markers to the map regardless of filters or region
                addAdminMarkersToMap();
            } else {
                // TODO: 29/11/2016 check here if the luke_camera has moved enough to get new stuff from the DB or not
                this.visibleRegion = this.googleMap.getProjection().getVisibleRegion();
                if (this.minDateInMs > 0) {
                    addSubmissionsToMap(this.visibleRegion);
                } else {
                    addSubmissionsToMap(this.visibleRegion);
                }
                addAdminMarkersToMap();
            }
        }
        // TODO: 02/12/2016 ungroup markers
/*
        if (this.googleMap.getCameraPosition().zoom < 5) {
        }
*/
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: connection failed");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected: connected to google api");

        if (ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getMainActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            getMainActivity().checkPermissions();
            return;
        }
        this.lastKnownLoc = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (this.lastKnownLoc != null) {
            this.lastLoc = this.lastKnownLoc;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended: google api connection suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLoc = location;
    }

    /**
     * Provides objects possibility to listen to OnCameraIdle events by calling
     * {@link #registerListener(OnCameraIdleListener listener)}
     */
    class CompositeOnCameraIdleListener implements OnCameraIdleListener {
        private List<OnCameraIdleListener> registeredListeners = new ArrayList<>();

        /**
         * Adds OnCameraIdleListener type object to the <code>List<OnCameraIdleListener> registeredListeners</code>.
         *
         * @param listener OnCameraIdleListener type object
         */
        void registerListener(OnCameraIdleListener listener) {
            this.registeredListeners.add(listener);
        }

        @Override
        public void onCameraIdle() {
            // loop through listeners and call their onCameraIdle method()
            for (int i = 0; i < this.registeredListeners.size(); i++) {
                this.registeredListeners.get(i).onCameraIdle();
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
            this.registeredListeners.add(listener);
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            // loop through listeners and call their onCameraIdle method()
            for (int i = 0; i < this.registeredListeners.size(); i++) {
                this.registeredListeners.get(i).onMarkerClick(marker);
            }
            return false;
        }
    }

    /**
     * Custom renderer for the markers and clusters, makes it possible to change markers and clusters
     * colors and icons
     */
    private class MarkerRenderer extends DefaultClusterRenderer<ClusterMarker> {
        private final IconGenerator mIconGenerator;
        private ShapeDrawable mColoredCircleBackground;
        private SparseArray mIcons = new SparseArray();
        private final float mDensity;

        MarkerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
            super(context, map, clusterManager);
            this.mDensity = context.getResources().getDisplayMetrics().density;
            this.mIconGenerator = new IconGenerator(context);
            this.mIconGenerator.setContentView(this.makeSquareTextView(context));
            this.mIconGenerator.setTextAppearance(com.google.maps.android.R.style.amu_ClusterIcon_TextAppearance);
            this.mIconGenerator.setBackground(this.makeClusterBackground(R.color.quill_gray));
        }

        /**
         * Called before a cluster item is rendered, changes marker color based on marker type
         *
         * @param item          The clicked cluster marker
         * @param markerOptions Setup object for the marker
         */
        @Override
        protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
            // change marker color based on the marker values
            if (!item.getAdminMarkerTitle().isEmpty()) {
                BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                markerOptions.icon(markerDescriptor);
            } else if (item.getPositive().equals("true")) {
                BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                markerOptions.icon(markerDescriptor);
            } else if (item.getPositive().equals("false")) {
                BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                markerOptions.icon(markerDescriptor);
            } else if (item.getPositive().equals("neutral")) {
                BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                markerOptions.icon(markerDescriptor);
            }
        }

        @Override
        protected void onClusterItemRendered(ClusterMarker clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
        }

        /**
         * Called before a cluster is rendered, checks through the cluster and based on contents
         * colors the cluster and it's borders
         *
         * @param cluster       The cluster that's going to be rendered
         * @param markerOptions Options object for the cluster
         */
        @Override
        protected void onBeforeClusterRendered(Cluster<ClusterMarker> cluster, MarkerOptions markerOptions) {
            // set default cluster border color
            this.mIconGenerator.setBackground(this.makeClusterBackground(R.color.storm_dust_gray));
            // check if cluster has admin marker inside and change circle outline color if it has
            for (ClusterMarker marker : cluster.getItems()) {
                if (!marker.getAdminMarkerTitle().isEmpty()) {
                    this.mIconGenerator.setBackground(this.makeClusterBackground(R.color.super_red));
                    break;
                }
            }
            int clusterColor;
            findElementWithMostOccurrences(cluster);
            // Set cluster color based on what items there's the most
            switch (findElementWithMostOccurrences(cluster)) {
                case POSITIVE:
                    clusterColor = getMainActivity().getContextCompatColor(R.color.marker_positive);
                    break;
                case NEUTRAL:
                    clusterColor = getMainActivity().getContextCompatColor( R.color.marker_neutral);
                    break;
                case NEGATIVE:
                    clusterColor = getMainActivity().getContextCompatColor(R.color.marker_negative);
                    break;
                default:
                    clusterColor = getMainActivity().getContextCompatColor( R.color.marker_neutral);
                    break;
            }

            int bucket = this.getBucket(cluster);
            //BitmapDescriptor descriptor = this.mIcons.get(bucket);
            this.mColoredCircleBackground.getPaint().setColor(clusterColor);
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(
                    this.mIconGenerator.makeIcon(this.getClusterText(bucket)));
            this.mIcons.put(bucket, descriptor);
            markerOptions.icon(descriptor);
        }

        /**
         * Finds the element type with most occurrences in the cluster
         *
         * @param cluster Cluster of SubmissionMarkers
         * @return Constants.markerCategories enum value matching the most common elements
         */
        private Constants.markerCategories findElementWithMostOccurrences(Cluster<ClusterMarker> cluster) {
            int negative = 0;
            int neutral = 0;
            int positive = 0;

            double clusterSize = ((double) cluster.getSize()) / 2;
            for (ClusterMarker marker : cluster.getItems()) {
                switch (marker.getPositive()) {
                    case "false":
                        negative++;
                        break;
                    case "neutral":
                        neutral++;
                        break;
                    case "true":
                        positive++;
                        break;
                }
                if (negative > clusterSize || neutral > clusterSize || positive > clusterSize) {
                    break;
                }
            }

            int biggest = Math.max(negative, Math.max(neutral, positive));
            if (neutral == biggest) {
                return Constants.markerCategories.NEUTRAL;
            } else if (negative == biggest) {
                return Constants.markerCategories.NEGATIVE;
            } else if (positive == biggest) {
                return Constants.markerCategories.POSITIVE;
            } else {
                return Constants.markerCategories.NEUTRAL;
            }
        }

        private SquareTextView makeSquareTextView(Context context) {
            SquareTextView squareTextView = new SquareTextView(context);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -2);
            squareTextView.setLayoutParams(layoutParams);
            // changed text
            squareTextView.setId(com.google.maps.android.R.id.amu_text);
            int twelveDpi = (int) (12.0F * this.mDensity);
            squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
            return squareTextView;
        }

        /**
         * Defines the cluster background, including outline, shape and color.
         *
         * @param borderColor Color of the cluster border
         * @return Returns type <code>LayerDrawable</code> background for the cluster
         */
        private LayerDrawable makeClusterBackground(int borderColor) {
            // Outline color
            int clusterOutlineColor = getMainActivity().getContextCompatColor( borderColor);
            this.mColoredCircleBackground = new ShapeDrawable(new OvalShape());
            ShapeDrawable outline = new ShapeDrawable(new OvalShape());
            outline.getPaint().setColor(clusterOutlineColor);
            LayerDrawable background = new LayerDrawable(
                    new Drawable[]{outline, this.mColoredCircleBackground});
            int strokeWidth = (int) (this.mDensity * 3.0F);
            background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
            return background;
        }
    }
}
