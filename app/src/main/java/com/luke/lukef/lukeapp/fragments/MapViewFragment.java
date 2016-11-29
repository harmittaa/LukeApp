package com.luke.lukef.lukeapp.fragments;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Submission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Handles the Map view, fetches submission
 */
public class MapViewFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, OnCameraIdleListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {
    private static final String TAG = "MapViewFragment";
    private View fragmentView;
    Location lastLoc;
    Location lastKnownLoc;
    GoogleMap googleMap;
    private MapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    LatLng currentCameraPosition;
    Button testbutton;
    Location longPressLoc;

    public Location getLastLoc() {
        if (longPressLoc == null) {
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
        } else {
            return longPressLoc;
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.MAP_CAMERA);
        setupGoogleMap();

        connectToGoogleApi();
        createLocationRequest();

        if (fragmentView != null) {
            ViewGroup parent = (ViewGroup) fragmentView.getParent();
            if (parent != null)
                parent.removeView(fragmentView);
        }
        try {
            fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
            mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.googleMapFragment);
            mapFragment.getMapAsync(this);
        } catch (InflateException e) {
        /* map is already there, just return view as it is  */
            mapFragment.getMapAsync(this);
        }
        testbutton = (Button) fragmentView.findViewById(R.id.button2);
        testbutton.setOnClickListener(this);
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /*Log.e(TAG, "onDestroyView: ondestroy called" );
        MapFragment f = (MapFragment) getChildFragmentManager()
                .findFragmentById(R.id.googleMapFragment);
        if (f != null) {
            Log.e(TAG, "onDestroyView: removing map fragment");
            getFragmentManager().beginTransaction().remove(f).commit();
        } else {
            Log.e(TAG, "onPause: no fragment found" );
        }*/
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button2:
                GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
                    Bitmap bitmap;

                    @Override
                    public void onSnapshotReady(Bitmap snapshot) {
                        bitmap = snapshot;
                        try {
                            FileOutputStream out = new FileOutputStream("/mnt/sdcard/Download/TeleSensors.png");
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                googleMap.snapshot(callback);

        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
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

        Location lastKnownLocation = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        this.lastKnownLoc = lastKnownLocation;

    }

    private void mapPinTest(Location l) {

    }

    private void zoomMap() {
        // TODO: 27/11/2016 Check permission, so no crash
        /*CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(getLastLoc().getLatitude(), getLastLoc().getLongitude()));
        CameraUpdate cu = CameraUpdateFactory.zoomTo(15);
        googleMap.moveCamera(center);
        googleMap.animateCamera(cu);

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));*/

        if (getLastLoc() != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(getLastLoc().getLatitude(), getLastLoc().getLongitude()))      // Sets the center of the map to Mountain View
                    .zoom(17)                  // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            currentCameraPosition = googleMap.getCameraPosition().target;
        }
    }

    private void connectToGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(getMainActivity()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Function that has anonymous runnable class which fetches all reports from the server.
     * Creates submission objects from JSON fetched from the server and adds them to the list.
     */
    private void getSubmissions() {
        Log.e(TAG, "getSubmissions: be starting");

        Runnable getSubmissions = new Runnable() {
            String jsonString;

            @Override
            public void run() {
                try {

                    // Gets the center of current map
                    Log.e(TAG, "Center is: lat" + lastLoc.getLatitude() + " and long " + lastLoc.getLongitude());
                    URL getReportsUrl = new URL("http://www.balticapp.fi/lukeA/report?long=" + lastLoc.getLongitude() + "?lat=" + lastLoc.getLatitude());
                    HttpURLConnection httpURLConnection = (HttpURLConnection) getReportsUrl.openConnection();
                    if (httpURLConnection.getResponseCode() == 200) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        jsonString = "";
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line + "\n");
                        }
                        bufferedReader.close();
                        jsonString = stringBuilder.toString();
                        JSONObject jsonObject;
                        JSONArray jsonArray;
                        List<Submission> submissions = new ArrayList<>();
                        List<Object> submissionCategoryIdList = new ArrayList<>();
                        try {
                            // make new JSONArray from the server's reply
                            jsonArray = new JSONArray(jsonString);
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    jsonObject = jsonArray.getJSONObject(i);

                                    // parse Submission's categories
                                    for (int j = 0; j < jsonObject.getJSONArray("categoryId").length(); j++) {
                                        submissionCategoryIdList.add(jsonObject.getJSONArray("categoryId").get(i));
                                        Log.e(TAG, "Category parse: " + submissionCategoryIdList.add(jsonObject.getJSONArray("categoryId").get(i)));
                                    }

                                    Bitmap image;
                                    Location location = new Location("");
                                    location.setLongitude(jsonObject.getDouble("longitude"));
                                    location.setLatitude(jsonObject.getDouble("latitude"));

                                    // TODO: 25/11/2016 Once images are implemented, create submission objects with Images
                                    //   submissions.add(new Submission(jsonObject.getString("id"), jsonObject.getString("title"), submissionCategoryIdList, jsonObject.getString("date"),
                                    //                    jsonObject.getString("description"), image, location));

                                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                                    Date date = format.parse(jsonObject.getString("date"));

                                    // submissions.add(new Submission(getMainActivity().getApplicationContext(), submissionCategoryIdList, date, jsonObject.getString("description"), location));
                                }
                                addSubmissionsToMap(submissions);

                            } else {
                                // TODO: 25/11/2016 No submissions, show info to user
                                Log.e(TAG, "No submissions");
                            }

                            // TODO: 25/11/2016 Handle exceptions
                        } catch (JSONException e) {
                            Log.e(TAG, "onPostExecute: ", e);
                        } catch (ParseException e) {
                            Log.e(TAG, "run: ERROR ", e);
                        }
                    } else {
                        // TODO: 25/11/2016 Show error when responsecode is not 200
                        Log.e(TAG, "Responsecode = " + httpURLConnection.getResponseCode());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "run: EXCEPTION", e);
                    Log.e(TAG, "doInBackground: ", e);
                }
            }
        };

        Thread thread = new Thread(getSubmissions);
        thread.start();
    }

    /**
     * Parses through provided list of submissions, creates OverlayItems and adds them to the map
     *
     * @param submissions List of Submission objects
     */
    private void addSubmissionsToMap(List<Submission> submissions) {
        /*
        List<OverlayItem> overlayItemsList = new ArrayList();
        // go through the submissions list, create OverlayItem objects and set the markers
        for (Submission s : submissions) {
            OverlayItem overlayItem = new OverlayItem("", "", new GeoPoint(s.getLocation().getLatitude(), s.getLocation().getLongitude()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                overlayItem.setMarker(getMainActivity().getDrawable(android.R.drawable.btn_star));
            }
            overlayItemsList.add(overlayItem);
        }
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(overlayItemsList,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        PopupMaker popMaker = new PopupMaker(getMainActivity());
                        popMaker.createPopupTest();
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, getMainActivity());
        mOverlay.setFocusItemsOnTap(true);
        map.getOverlays().add(mOverlay);*/

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.setOnCameraIdleListener(this);
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
        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                longPressLoc = new Location("jippii");
                longPressLoc.setLatitude(latLng.latitude);
                longPressLoc.setLongitude(latLng.longitude);
                longPressLoc.setAltitude(0);
                getMainActivity().makeSubmission();
            }
        });
    }

    /**
     * Gets called when user has stopped moving the map
     */
    @Override
    public void onCameraIdle() {
        // TODO: 27/11/2016 Make a call to fetch new submissions from the server or populate the map based on initial request
        Log.e(TAG, "onCameraIdle: setting current camera position");
        currentCameraPosition = googleMap.getCameraPosition().target;
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
            return;
        }
        this.lastKnownLoc = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (this.lastKnownLoc != null) {
            this.lastLoc = this.lastKnownLoc;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);

        getSubmissions();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended: google api connection suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLoc = location;
    }
}
