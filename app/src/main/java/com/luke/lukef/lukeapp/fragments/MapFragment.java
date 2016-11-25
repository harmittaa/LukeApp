package com.luke.lukef.lukeapp.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.tools.PopupMaker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.BufferedReader;
import java.io.IOException;
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
public class MapFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MapFragment";
    private View fragmentView;
    private Button newSubmissionButton;
    private Button leaderboardButton;
    private MapView map;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: MAP fragment");
        fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
        //pointOfInterestButton = (Button) fragmentView.findViewById(R.id.poi_button);
        newSubmissionButton = (Button) fragmentView.findViewById(R.id.new_submission_button);
        leaderboardButton = (Button) fragmentView.findViewById(R.id.leaderboard_button);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.MAP_CAMERA);
        setupOSMap();
        getSubmissions();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /*case R.id.poi_button:
                //getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_POINT_OF_INTEREST);
                PopupMaker pm = new PopupMaker(getMainActivity());
                pm.createPopupTest();
                break;*/
            case R.id.new_submission_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_NEW_SUBMISSION);
                break;
            case R.id.leaderboard_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_LEADERBOARD);
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
//        pointOfInterestButton.setOnClickListener(this);
        newSubmissionButton.setOnClickListener(this);
        leaderboardButton.setOnClickListener(this);
    }

    /**
     * Setup method for open street map in this fragment
     * Enables touch controls
     * Sets starting position and zooms in
     */
    private void setupOSMap() {
        //init map
        map = (MapView) fragmentView.findViewById(R.id.mapMain);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //enable pinch zoom
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        //get current phone position and zoom to location
        LocationManager lm = (LocationManager) getMainActivity().getSystemService(Context.LOCATION_SERVICE);
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
 /*       Location lastLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        IMapController mapController = map.getController();
        mapController.setZoom(100);
        GeoPoint startPoint = new GeoPoint(lastLoc.getLatitude(), lastLoc.getLongitude());
        mapController.setCenter(startPoint);

        mapPinTest(lastLoc);

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getMainActivity()),map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay); */

        CompassOverlay mCompassOverlay = new CompassOverlay(getMainActivity(), new InternalCompassOrientationProvider(getMainActivity()), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(mCompassOverlay);

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(getMainActivity(), map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);
        /*scale bar, looks wonky
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        //play around with these values to get the location on screen in the right place for your applicatio
        mScaleBarOverlay.setScaleBarOffset(map.getWidth(), 10);
        map.getOverlays().add(mScaleBarOverlay);*/
    }

    private void mapPinTest(Location l) {
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        OverlayItem newOI = new OverlayItem("", "", new GeoPoint(l.getLatitude(), l.getLongitude()));
        //PIN DRAWABLE CAN BE CHANGED, this can be used to make different colored pins for categories / submission types
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            newOI.setMarker(getMainActivity().getDrawable(android.R.drawable.btn_star));
            newOI.setMarkerHotspot(OverlayItem.HotspotPlace.RIGHT_CENTER);
        }
        items.add(newOI); // Lat/Lon decimal degrees

        //the overlay
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        //do something
                        //getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_POINT_OF_INTEREST);
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
        map.getOverlays().add(mOverlay);
    }


    /**
     * Function that has anonymous runnable class which fetches all reports from the server.
     * Creates submission objects from JSON fetched from the server and adds them to the list.
     */
    private void getSubmissions() {
        Runnable getSubmissions = new Runnable() {
            String jsonString;

            @Override
            public void run() {
                try {
                    // Gets the center of current map
                    IGeoPoint currentCenterPoint = map.getMapCenter();
                    Log.e(TAG, "Center is: lat" + currentCenterPoint.getLatitude() + " and long " + currentCenterPoint.getLongitude());
                    URL getReportsUrl = new URL("http://www.balticapp.fi/lukeA/report?long=" + currentCenterPoint.getLongitude() + "?lat=" + currentCenterPoint.getLatitude());
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

                                    submissions.add(new Submission(submissionCategoryIdList, date, jsonObject.getString("description"), location));
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
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: ", e);
                }
            }
        };

        Thread thread = new Thread(getSubmissions);
        thread.start();
    }

    /**
     * Parses through provided list of submissions, creates OverlayItems and adds them to the map
     * @param submissions List of Submission objects
     */
    private void addSubmissionsToMap(List<Submission> submissions) {
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
        map.getOverlays().add(mOverlay);

    }
}
