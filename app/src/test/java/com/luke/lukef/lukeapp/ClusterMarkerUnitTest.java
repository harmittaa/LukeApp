package com.luke.lukef.lukeapp;

import com.google.android.gms.maps.model.LatLng;
import com.luke.lukef.lukeapp.model.ClusterMarker;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link ClusterMarker}.
 */
@RunWith(JUnit4.class)
public class ClusterMarkerUnitTest {

    /**
     * Tests {@link ClusterMarker} constructor.
     */
    @Test
    public void testConstructor() {
        ClusterMarker c = new ClusterMarker("id", 1, 2, "title", "positive");
        Assert.assertNotNull(c);
    }

    /**
     * Tests {@link ClusterMarker} constructor values:
     * <li>{@link ClusterMarker#adminMarkerTitle}</li>
     * <li>{@link ClusterMarker#submissionId}</li>
     * <li>{@link ClusterMarker#markerPosition}</li>
     * <li>{@link ClusterMarker#positive}</li>
     */
    @Test
    public void testConstructorValues() {
        String submissionId = "id";
        double lat = 23.2333;
        double lng = 21.0000;
        String adminMarkerTitle = "title";
        String positive = "yep";
        ClusterMarker c = new ClusterMarker(submissionId, lat, lng, adminMarkerTitle, positive);
        Assert.assertSame(submissionId, c.getSubmissionId());
        Assert.assertEquals(lat, c.getPosition().latitude);
        Assert.assertEquals(lng, c.getPosition().longitude);
        Assert.assertSame(adminMarkerTitle, c.getAdminMarkerTitle());
        Assert.assertSame(positive, c.getPositive());
    }

    /**
     * Tests that the LatLng object of {@link ClusterMarker} is created correctly.
     */
    @Test
    public void testLocation() {
        double lat = 23.2333;
        double lng = 21.0000;
        LatLng latLng = new LatLng(lat, lng);

        ClusterMarker c = new ClusterMarker("", lat, lng, "", "");
        Assert.assertEquals(latLng.latitude, c.getPosition().latitude);
        Assert.assertEquals(latLng.longitude, c.getPosition().longitude);
    }


}
