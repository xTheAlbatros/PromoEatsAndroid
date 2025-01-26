package com.example.promoeatsandroid.models;

import java.io.Serializable;

public class RestaurantRequest implements Serializable {
    private Location location;
    private int range;

    public RestaurantRequest(Location location, int range) {
        this.location = location;
        this.range = range;
    }

    public Location getLocation() {
        return location;
    }

    public int getRange() {
        return range;
    }

    @Override
    public String toString() {
        return "RestaurantRequest{" +
                "location=" + location +
                ", range=" + range +
                '}';
    }
}
