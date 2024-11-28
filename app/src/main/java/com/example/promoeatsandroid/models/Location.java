package com.example.promoeatsandroid.models;

import java.io.Serializable;

public class Location implements Serializable {
    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "Latitude: " + latitude + ", Longitude: " + longitude;
    }
}
