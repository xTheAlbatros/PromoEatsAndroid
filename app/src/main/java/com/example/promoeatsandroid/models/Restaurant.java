package com.example.promoeatsandroid.models;

import com.google.gson.annotations.SerializedName;

public class Restaurant {
    private int id;
    private String name;
    private String phone;
    private String email;

    @SerializedName("webside") // Mapowanie z backendu
    private String website;
    private Location location;

    private boolean isExpanded;

    // Dodajemy nowe pole isFavourite
    private boolean isFavourite;

    // Gettery i settery
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    // Dodajemy getter i setter dla isFavourite
    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
}
