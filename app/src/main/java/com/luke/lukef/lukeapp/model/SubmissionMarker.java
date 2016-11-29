package com.luke.lukef.lukeapp.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Used to create the markers objects suitable for Clustering
 */
public class SubmissionMarker implements ClusterItem {
    private final LatLng markerPosition;

    public SubmissionMarker(String submission_id, double lat, double lng) {
        this.markerPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return markerPosition;
    }
}
