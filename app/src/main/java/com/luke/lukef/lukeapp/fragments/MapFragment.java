package com.luke.lukef.lukeapp.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.tools.PopupMaker;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureDetector;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MapFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MapFragment";
    private View fragmentView;
    private Button pointOfInterestButton;
    private Button leaderboardButton;
    private MapView map;


    public String performPostCall(final String requestURL, final HashMap<String, String> postDataParams) {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                URL url;
                String response = "";
                try {
                    url = new URL(requestURL);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty(getString(R.string.authorization), getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                    conn.setRequestProperty(getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);


                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getPostDataString(postDataParams));

                    writer.flush();
                    writer.close();
                    os.close();
                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == 200) {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            response += line + "\n";
                        }
                        Log.e(TAG, "ACTUAL RESPONSE: " + response);
                    } else {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                        while ((line = br.readLine()) != null) {
                            response += line + "\n";
                        }
                        Log.e(TAG, "actual ERROR CODE: " + responseCode);
                        Log.e(TAG, "ACTUAL ERROR: " + response);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "performPostCall: ", e);
                }

            }
        };
        Thread t = new Thread(r);
        t.start();
        return "YEEEEE";

    };

    @NonNull
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: MAP fragment");
        fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
        //pointOfInterestButton = (Button) fragmentView.findViewById(R.id.poi_button);
        leaderboardButton = (Button) fragmentView.findViewById(R.id.leaderboard_button);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.MAP_CAMERA);
        setupOSMap();

        HashMap<String, String> params = new HashMap<>();
        params.put("title", "TestPattern");
        params.put("reporGain", "100");
        params.put("upvoteGain", "10");
        params.put("downvoteGain", "2");
        params.put("active", "true");
        Log.e(TAG, "onCreate:  BEFORE POST REQUEST!");
        performPostCall("http://www.balticapp.fi/lukeA/experience/create", params);
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
        Location lastLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        IMapController mapController = map.getController();
        mapController.setZoom(100);
        GeoPoint startPoint = new GeoPoint(lastLoc.getLatitude(), lastLoc.getLongitude());
        mapController.setCenter(startPoint);

        mapPinTest(lastLoc);

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getMainActivity()),map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);

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

    private void mapPinTest(Location l){
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        OverlayItem newOI = new OverlayItem("", "", new GeoPoint(l.getLatitude(),l.getLongitude()));
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
}
