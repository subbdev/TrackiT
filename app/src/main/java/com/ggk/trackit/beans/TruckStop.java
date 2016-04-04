package com.ggk.trackit.beans;

public class TruckStop {
    private String name;
    private String city;
    private String state;
    private String country;
    private String zip;
    private double lat;
    private double lng;
    private String rawLine1;
    private String rawLine2;
    private String rawLine3;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getRawLine1() {
        return rawLine1;
    }

    public void setRawLine1(String rawLine1) {
        this.rawLine1 = rawLine1;
    }

    public String getRawLine2() {
        return rawLine2;
    }

    public void setRawLine2(String rawLine2) {
        this.rawLine2 = rawLine2;
    }

    public String getRawLine3() {
        return rawLine3;
    }

    public void setRawLine3(String rawLine3) {
        this.rawLine3 = rawLine3;
    }
}
