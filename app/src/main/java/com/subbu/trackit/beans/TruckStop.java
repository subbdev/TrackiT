package com.subbu.trackit.beans;

/**
 * Created by Subrahmanyam.yepuri on 31-03-2016.
 */
public class TruckStop {
    String name;
    String city;
    String state;
    String country;
    String zip;
    double lat;
    double lng;
    String rawLine1;
    String rawLine2;
    String rawLine3;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
