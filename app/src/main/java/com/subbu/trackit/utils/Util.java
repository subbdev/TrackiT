package com.subbu.trackit.utils;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Subrahmanyam.yepuri on 01-04-2016.
 */
public class Util {
    public static long radiusToZoom(float radius) {
        double scale = radius*1609.34 / 500;
        return Math.round(16 - Math.log(scale) / Math.log(2));
    }
    public static int getCurrentRadius(GoogleMap mMap, float dratio)
    {
        LatLngBounds llBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        float[] test = new float[3];
        Location.distanceBetween(llBounds.northeast.latitude, llBounds.northeast.longitude, llBounds.southwest.latitude, llBounds.southwest.longitude, test);
        int radius = (int) (((test[0]/Math.sqrt(1+dratio))* 0.00062137)/2)+25;
        return radius;
    }
}
