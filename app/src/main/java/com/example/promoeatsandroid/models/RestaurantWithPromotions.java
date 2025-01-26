package com.example.promoeatsandroid.models;

import java.util.List;

public class RestaurantWithPromotions {
    private Restaurant restaurant;
    private List<Promotion> promotions;

    public RestaurantWithPromotions(Restaurant restaurant, List<Promotion> promotions) {
        this.restaurant = restaurant;
        this.promotions = promotions;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<Promotion> promotions) {
        this.promotions = promotions;
    }
}
