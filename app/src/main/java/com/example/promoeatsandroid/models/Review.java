package com.example.promoeatsandroid.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    private int rate;
    private String comment;

    @SerializedName("restaurants")
    private Restaurant restaurant;

    @SerializedName("created_time")
    private String createdTime;

    public Review(int rate, String comment, Restaurant restaurant) {
        this.rate = rate;
        this.comment = comment;
        this.restaurant = restaurant;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
}
