package com.luke.lukef.lukeapp.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Used to create the markers objects suitable for Clustering
 */
public class ClusterMarker implements ClusterItem {
    private final LatLng markerPosition;
    private final String submissionId;
    private final String adminMarkerTitle;
    private final String positive;

    public ClusterMarker(String submission_id, double lat, double lng, String adminMarkerTitle, String positive) {
        this.submissionId = submission_id;
        this.adminMarkerTitle = adminMarkerTitle;
        this.markerPosition = new LatLng(lat, lng);
        this.positive = positive;
    }

    @Override
    public LatLng getPosition() {
        return markerPosition;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getAdminMarkerTitle() {
        return adminMarkerTitle;
    }

    public String getPositive() {
        return positive;
    }
}