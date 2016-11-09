package com.luke.lukef.lukeapp.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.luke.lukef.lukeapp.tools.PopupMaker;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MapFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MapFragment";
    private View fragmentView;
    private Button pointOfInterestButton;
    private Button newSubmissionButton;
    private Button leaderboardButton;
    private MapView map;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: MAP fragment");
        fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
        pointOfInterestButton = (Button) fragmentView.findViewById(R.id.poi_button);
        newSubmissionButton = (Button) fragmentView.findViewById(R.id.new_submission_button);
        leaderboardButton = (Button) fragmentView.findViewById(R.id.leaderboard_button);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.MAP_CAMERA);
        setupOSMap();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.poi_button:
                //getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_POINT_OF_INTEREST);
                PopupMaker pm = new PopupMaker(getMainActivity());
                pm.createPopupTest();
                break;
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
        pointOfInterestButton.setOnClickListener(this);
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
        Location lastLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        IMapController mapController = map.getController();
        mapController.setZoom(100);
        GeoPoint startPoint = new GeoPoint(lastLoc.getLatitude(), lastLoc.getLongitude());
        mapController.setCenter(startPoint);

        mapPinTest(lastLoc);
    }

    private void mapPinTest(Location l){
        //your items
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(new OverlayItem("Title", "Description", new GeoPoint(l.getLatitude(),l.getLongitude()))); // Lat/Lon decimal degrees

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
