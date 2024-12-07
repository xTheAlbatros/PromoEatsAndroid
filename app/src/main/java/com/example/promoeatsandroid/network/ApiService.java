package com.example.promoeatsandroid.network;

import com.example.promoeatsandroid.models.AuthResponse;
import com.example.promoeatsandroid.models.Category;
import com.example.promoeatsandroid.models.LoginRequest;
import com.example.promoeatsandroid.models.User;
import com.example.promoeatsandroid.models.Restaurant;
import com.example.promoeatsandroid.models.Promotion;
import com.example.promoeatsandroid.models.Review;
import com.example.promoeatsandroid.models.Images;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body User user);

    @POST("api/auth/authenticate")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @GET("api/auth/logout")
    Call<Void> logout(@Header("Authorization") String token);

    @GET("api/restaurants")
    Call<List<Restaurant>> getRestaurants(@Header("Authorization") String token);

    @GET("api/restaurant/{id}/promotions")
    Call<List<Promotion>> getPromotions(@Header("Authorization") String token, @Path("id") int restaurantId);

    @GET("api/restaurant/{id}/reviews")
    Call<List<Review>> getReviews(@Header("Authorization") String token, @Path("id") int restaurantId);

    @POST("api/restaurant/review")
    Call<Void> addReview(@Header("Authorization") String token, @Body Review review);

    @GET("api/restaurant/favourites")
    Call<List<Restaurant>> getFavourites(@Header("Authorization") String token);

    @POST("api/restaurant/{id}/favourite")
    Call<Void> addFavourite(@Header("Authorization") String token, @Path("id") int restaurantId);

    @DELETE("api/restaurant/{id}/favourite")
    Call<Void> deleteFavourite(@Header("Authorization") String token, @Path("id") int restaurantId);

    @GET("api/promotion/{id}/images")
    Call<List<Images>> getImagesForPromotion(@Header("Authorization") String token, @Path("id") int promotionId
    );

    @POST("api/restaurants/location/categories")
    Call<List<Restaurant>> getRestaurantRestaurantsByLocationAndCategories(
            @Header("Authorization") String token,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("range") int range,
            @Body List<String> categories
    );

    @GET("api/restaurants/location")
    Call<List<Restaurant>> getRestaurantRestaurantsByLocation(
            @Header("Authorization") String token,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("range") int range
    );

    @GET("api/categories")
    Call<List<Category>> getAllCategories(@Header("Authorization") String token);

}
