package com.luke.lukef.lukeapp.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import com.luke.lukef.lukeapp.SubmissionDatabase;
import com.luke.lukef.lukeapp.model.SubmissionMarker;
import com.luke.lukef.lukeapp.tools.PopupMaker;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the Map view, fetches submissions, populates map with Submissions and Admin markers
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
                addAdminMarkersToMap();
            } else {
                // TODO: 29/11/2016 check here if the camera has moved enough to get new stuff from the DB or not
                this.visibleRegion = this.googleMap.getProjection().getVisibleRegion();
                addSubmissionsToMap(this.visibleRegion);
                addAdminMarkersToMap();
            }
        }

        if (this.googleMap.getCameraPosition().zoom < 5) {

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
            while (queryCursor.moveToNext()) {
                if (!this.submissionMarkerIdList.contains(queryCursor.getString(queryCursor.getColumnIndexOrThrow("admin_marker_id")))) {
                    SubmissionMarker adminMarker = new SubmissionMarker(
                            queryCursor.getString(queryCursor.getColumnIndexOrThrow("admin_marker_id")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("admin_marker_latitude")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("admin_marker_longitude")),
                            queryCursor.getString(queryCursor.getColumnIndexOrThrow("admin_marker_title")),
                            "");
                    this.submissionMarkerIdList.add(queryCursor.getString(queryCursor.getColumnIndexOrThrow("admin_marker_id")));
                    this.clusterManager.addItem(adminMarker);
                } else {
                    // Log.e(TAG, "addSubmissionsToMap: Submission already on the map");
                }
            }
            this.clusterManager.cluster();
        }
        submissionDatabase.closeDbConnection();
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
            //Log.e(TAG, "addSubmissionsToMap: Submissions amount is " + queryCursor.getCount());
            while (queryCursor.moveToNext()) {
                if (!this.submissionMarkerIdList.contains(queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_id")))) {
                    SubmissionMarker submissionMarker = new SubmissionMarker(
                            queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_id")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("submission_latitude")),
                            queryCursor.getDouble(queryCursor.getColumnIndexOrThrow("submission_longitude")),
                            "",
                            queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_positive")));
                    this.submissionMarkerIdList.add(queryCursor.getString(queryCursor.getColumnIndexOrThrow("submission_id")));
                    this.clusterManager.addItem(submissionMarker);
                }
            }
            this.clusterManager.cluster();
        }
        submissionDatabase.closeDbConnection();
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
        if (!submissionMarker.getAdminMarkerTitle().isEmpty()) {
            Log.e(TAG, "onClusterItemClick: TITLE OF THE MARKER IS " + submissionMarker.getAdminMarkerTitle());
        } else {
            PopupMaker popMaker = new PopupMaker(getMainActivity());
            popMaker.createPopupTest();
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "onMarkerClick: marker clicked");
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

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class MarkerRenderer extends DefaultClusterRenderer<SubmissionMarker> {
        private final IconGenerator mIconGenerator;
        private ShapeDrawable mColoredCircleBackground;
        private SparseArray<BitmapDescriptor> mIcons = new SparseArray();
        private final float mDensity;


        MarkerRenderer(Context context, GoogleMap map, ClusterManager<SubmissionMarker> clusterManager) {
            super(context, map, clusterManager);
            this.mDensity = context.getResources().getDisplayMetrics().density;
            this.mIconGenerator = new IconGenerator(context);
            this.mIconGenerator.setContentView(this.makeSquareTextView(context));
            this.mIconGenerator.setTextAppearance(com.google.maps.android.R.style.amu_ClusterIcon_TextAppearance);
            this.mIconGenerator.setBackground(this.makeClusterBackground(R.color.quill_gray));
        }

        @Override
        protected void onBeforeClusterItemRendered(SubmissionMarker item, MarkerOptions markerOptions) {
            // change marker color based on the marker values
            if (!item.getAdminMarkerTitle().isEmpty()) {
                BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
                markerOptions.icon(markerDescriptor);
            } else if (item.getPositive().equals("true")) {
                BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                markerOptions.icon(markerDescriptor);
            } else if (item.getPositive().equals("false")) {
                BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE);
                markerOptions.icon(markerDescriptor);
            } else if (item.getPositive().equals("neutral")) {
                BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                markerOptions.icon(markerDescriptor);
            }
        }

        @Override
        protected void onClusterItemRendered(SubmissionMarker clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<SubmissionMarker> cluster, MarkerOptions markerOptions) {
            // set default cluster border color
            this.mIconGenerator.setBackground(this.makeClusterBackground(R.color.storm_dust_gray));
            // check if cluster has admin marker inside and change circle outline color if it has
            for (SubmissionMarker marker : cluster.getItems()) {
                if (!marker.getAdminMarkerTitle().isEmpty()) {
                    this.mIconGenerator.setBackground(this.makeClusterBackground(R.color.super_red));
                    break;
                }
            }

            int clusterColor;
            Boolean mostOccurrences = findElementWithMostOccurrences(cluster);
            // Set cluster color based on what items there's the most
            if (mostOccurrences == null) {
                clusterColor = ContextCompat.getColor(getContext(), R.color.quill_gray);
            } else if (mostOccurrences) {
                clusterColor = ContextCompat.getColor(getContext(), R.color.shamrock);
            } else {
                clusterColor = ContextCompat.getColor(getContext(), R.color.bittersweet);
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
         * Finds the element type with most occurrences.
         *
         * @param cluster Cluster of SubmissionMarkers
         * @return Boolean.TRUE if there was mostly positives, Boolean.FALSE if negatives, <code>null</code> otherwise
         */
        private Boolean findElementWithMostOccurrences(Cluster<SubmissionMarker> cluster) {
            int negative = 0;
            int neutral = 0;
            int positive = 0;

            double clusterSize = ((double) cluster.getSize()) / 2;
            for (SubmissionMarker marker : cluster.getItems()) {
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
                return null;
            } else if (negative == biggest) {
                return Boolean.FALSE;
            } else if (positive == biggest) {
                return Boolean.TRUE;
            } else {
                return null;
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
         * Defines the cluster background, including outline, shape and colorl
         * @param borderColor Color of the cluster border
         * @return Returns type <code>LayerDrawable</code> background for the cluster
         */
        private LayerDrawable makeClusterBackground(int borderColor) {
            // Outline color
            int clusterOutlineColor = ContextCompat.getColor(getContext(), borderColor);

            this.mColoredCircleBackground = new ShapeDrawable(new OvalShape());
            ShapeDrawable outline = new ShapeDrawable(new OvalShape());
            outline.getPaint().setColor(clusterOutlineColor);
            LayerDrawable background = new LayerDrawable(
                    new Drawable[]{outline, this.mColoredCircleBackground});
            int strokeWidth = (int) (this.mDensity * 3.0F);
            background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
            return background;
        }


        /**
         * Generates the cluster Bitmaps based on color
         *
         * @param cluster The cluster for which the bitmap is generated, used to fetch the item count
         * @param color   The color that the bitmap should be
         * @return The coloured and numbered Bitmap for the cluster
         */
        Bitmap createCluster(Cluster cluster, int color) {
            IconGenerator mIconGenerator = new IconGenerator(getActivity());
            IconGenerator mClusterIconGenerator = new IconGenerator(getActivity());
            final Drawable clusterIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_circle);
            clusterIcon.setColorFilter(ContextCompat.getColor(getContext(), color), PorterDuff.Mode.SRC_ATOP);
            mClusterIconGenerator.setBackground(clusterIcon);
            //modify padding for one or two digit numbers
            if (cluster.getSize() < 10) {
                mClusterIconGenerator.setContentPadding(20, 10, 0, 0);
            } else {
                mClusterIconGenerator.setContentPadding(15, 10, 0, 0);
            }
            return mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
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
