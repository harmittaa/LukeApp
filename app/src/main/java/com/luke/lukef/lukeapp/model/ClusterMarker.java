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
